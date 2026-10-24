package kr.acorntracker;

import java.util.Arrays;
import java.util.function.Supplier;

public final class InventoryLedger {
   private final int[] slots;
   private int cursor;
   private long baseline;
   private long lastPacket;
   private boolean ready;
   private boolean dirty;
   private boolean suspended;
   private boolean uncertain;
   private TrackerEngine.Observation context;
   private final Supplier<TrackerEngine.Observation> contexts;

   public InventoryLedger(int size, Supplier<TrackerEngine.Observation> contexts) {
      this.slots = new int[size];
      this.contexts = contexts;
   }

   public boolean ready() {
      return this.ready;
   }

   public long total() {
      return Arrays.stream(this.slots).asLongStream().sum() + (long)this.cursor;
   }

   public void seed(int[] counts, int cursorCount) {
      Arrays.fill(this.slots, 0);
      System.arraycopy(counts, 0, this.slots, 0, Math.min(counts.length, this.slots.length));
      this.cursor = cursorCount;
      this.baseline = this.total();
      this.ready = true;
      this.dirty = false;
   }

   private void packet(long tick) {
      TrackerEngine.Observation observation = (TrackerEngine.Observation)this.contexts.get();
      this.context = this.dirty ? this.context.combine(observation) : observation;
      this.dirty = true;
      this.lastPacket = tick;
   }

   public void slot(int index, int count, long tick) {
      if (this.ready && index >= 0 && index < this.slots.length) {
         if (this.slots[index] != Math.max(0, count)) {
            this.packet(tick);
            this.slots[index] = Math.max(0, count);
         }
      }
   }

   public void cursor(int count, long tick) {
      if (this.ready) {
         if (this.cursor != Math.max(0, count)) {
            this.packet(tick);
            this.cursor = Math.max(0, count);
         }
      }
   }

   public void suspend(boolean value) {
      if (value) {
         this.uncertain = true;
      }

      this.suspended = value;
   }

   public void settle(long tick) {
      if (this.ready && !this.suspended) {
         if (!this.dirty) {
            this.uncertain = false;
         } else if (tick - this.lastPacket >= 2L) {
            long delta = this.total() - this.baseline;
            if (delta > 0L) {
               (this.uncertain ? this.context.asUnknown() : this.context).accept(delta);
            }

            this.baseline = this.total();
            this.dirty = false;
            this.uncertain = false;
         }
      }
   }

   public void clear() {
      this.ready = this.dirty = this.suspended = this.uncertain = false;
      Arrays.fill(this.slots, 0);
      this.cursor = 0;
   }
}
