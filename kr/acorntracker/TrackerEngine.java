package kr.acorntracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.LongConsumer;

public final class TrackerEngine {
   private final List<Interval> intervals = new ArrayList();
   private final Map<Integer, Stats> statistics = new TreeMap();
   private final Map<Long, Owner> owners = new HashMap();
   private long tick;
   private long unknown;
   private long failed;
   private long epoch;
   private int waitTicks = 10;
   private int confirmationTicks = 100;
   private int rollbackTicks = 200;
   private Interval current;

   public Map<Integer, Stats> statistics() {
      Map<Integer, Stats> result = new TreeMap();
      this.statistics.forEach((level, sx) -> {
         Stats copy = new Stats();
         copy.logs = sx.logs;
         copy.acorns = sx.acorns;
         copy.decidable = sx.decidable;
         copy.successes = sx.successes;
         copy.estimatedLogs = sx.estimatedLogs;
         copy.estimatedAcorns = sx.estimatedAcorns;
         copy.multiAwardLogs = sx.multiAwardLogs;
         result.put(level, copy);
      });

      for(Interval i : this.intervals) {
         if (!i.closed) {
            Stats s = (Stats)result.computeIfAbsent(i.fortune, (k) -> new Stats());
            long logs = i.mines.stream().filter((m) -> m.confirmed && !m.failed).count();
            long acorns = logs > 0L && !i.invalidated ? i.acorns : 0L;
            s.logs += logs;
            s.acorns += acorns;
            s.estimatedLogs += logs;
            s.estimatedAcorns += acorns;
         }
      }

      return Collections.unmodifiableMap(result);
   }

   public long unknown() {
      return this.unknown;
   }

   public long failed() {
      return this.failed;
   }

   public long epoch() {
      return this.epoch;
   }

   public int pending() {
      return (int)this.intervals.stream().filter((i) -> !i.closed).flatMap((i) -> i.mines.stream()).count();
   }

   public void configure(int wait, int confirmation, int rollback) {
      this.waitTicks = Math.max(1, wait);
      this.confirmationTicks = Math.max(this.waitTicks, confirmation);
      this.rollbackTicks = Math.max(this.waitTicks, rollback);
      this.current = null;
   }

   public void fortuneChanged() {
      this.current = null;
   }

   public void measurementInterrupted() {
      this.intervals.stream().filter((i) -> !i.closed).forEach((i) -> {
         i.overlap = true;
         i.invalidated = true;
      });
   }

   public void attempt(long position, int fortune, long baseline) {
      Owner previous = (Owner)this.owners.get(position);
      if (previous == null || previous.interval.closed || previous.mine.failed) {
         if (this.current == null || this.current.closed || this.current.fortune != fortune || this.tick > this.current.deadline()) {
            this.current = new Interval(fortune);
            this.intervals.add(this.current);
         }

         if (!this.current.mines.isEmpty()) {
            this.current.overlap = true;
         }

         for(Interval other : this.intervals) {
            if (other != this.current && !other.closed && this.tick <= other.deadline() && other.mines.stream().anyMatch((m) -> !m.failed && this.tick <= m.deadline)) {
               other.overlap = true;
               this.current.overlap = true;
            }
         }

         Mine mine = new Mine(position, this.tick, baseline, this.waitTicks);
         mine.deadline = this.tick + (long)this.confirmationTicks;
         this.current.mines.add(mine);
         this.owners.put(position, new Owner(this.current, mine));
         ++this.epoch;
      }
   }

   public void localFailure(long position) {
      this.intervals.stream().filter((i) -> !i.closed).forEach((i) -> i.mines.stream().filter((m) -> m.position == position && !m.confirmed && !m.failed).forEach((m) -> {
            m.failed = true;
            m.deadline = this.tick;
            ++this.failed;
         }));
   }

   public void serverBlock(long position, boolean tree, boolean air) {
      Owner owner = (Owner)this.owners.get(position);
      if (owner != null) {
         Interval interval = owner.interval;
         Mine mine = owner.mine;
         if (tree && mine.confirmed && !mine.failed) {
            mine.failed = true;
            ++this.failed;
            interval.invalidated = true;
            if (interval.closed) {
               this.uncount(interval);
               interval.invalidated = true;
               this.count(interval);
            }
         } else if (air && !mine.confirmed && !mine.failed && !interval.closed) {
            mine.confirmed = true;
            mine.deadline = this.tick + (long)this.waitTicks;
         }

         ++this.epoch;
      }
   }

   public void inventoryGain(long amount) {
      this.inventoryContext().accept(amount);
   }

   public Observation inventoryContext() {
      List<Interval> candidates = this.intervals.stream().filter((i) -> !i.closed && this.tick <= i.deadline() && i.mines.stream().anyMatch((m) -> !m.failed && this.tick <= m.deadline)).toList();
      return new Observation(candidates, false);
   }

   public void advance() {
      ++this.tick;

      for(Interval interval : this.intervals) {
         if (!interval.closed && this.tick > interval.deadline()) {
            interval.closed = true;
            interval.closedAt = this.tick;

            for(Mine mine : interval.mines) {
               if (!mine.confirmed && !mine.failed) {
                  mine.failed = true;
                  ++this.failed;
               }
            }

            this.count(interval);
            ++this.epoch;
         }
      }

      this.intervals.removeIf((i) -> {
         if (i.closed && this.tick - i.closedAt > (long)this.rollbackTicks) {
            for(Mine m : i.mines) {
               Owner owner = (Owner)this.owners.get(m.position);
               if (owner != null && owner.interval == i) {
                  this.owners.remove(m.position);
               }
            }

            return true;
         } else {
            return false;
         }
      });
   }

   private void count(Interval interval) {
      Stats stats = (Stats)this.statistics.computeIfAbsent(interval.fortune, (k) -> new Stats());
      long logs = interval.mines.stream().filter((m) -> m.confirmed && !m.failed).count();
      long acorns = !interval.invalidated && logs != 0L ? interval.acorns : 0L;
      if (acorns == 0L && interval.acorns > 0L) {
         this.unknown += interval.acorns;
         interval.acorns = 0L;
      }

      boolean exact = !interval.overlap && !interval.invalidated && logs == 1L;
      interval.countedLogs = logs;
      interval.countedAcorns = acorns;
      interval.countedDecidable = exact ? 1L : 0L;
      interval.countedSuccesses = exact && acorns > 0L ? 1L : 0L;
      interval.countedMulti = exact && acorns > 1L ? 1L : 0L;
      stats.logs += logs;
      stats.acorns += acorns;
      stats.decidable += interval.countedDecidable;
      stats.successes += interval.countedSuccesses;
      stats.multiAwardLogs += interval.countedMulti;
      if (!exact) {
         stats.estimatedLogs += logs;
         stats.estimatedAcorns += acorns;
      }

   }

   private void uncount(Interval i) {
      Stats s = (Stats)this.statistics.get(i.fortune);
      s.logs -= i.countedLogs;
      s.acorns -= i.countedAcorns;
      s.decidable -= i.countedDecidable;
      s.successes -= i.countedSuccesses;
      s.multiAwardLogs -= i.countedMulti;
      if (i.countedDecidable == 0L) {
         s.estimatedLogs -= i.countedLogs;
         s.estimatedAcorns -= i.countedAcorns;
      }

      ++this.epoch;
   }

   public void endSession() {
      for(Interval interval : this.intervals) {
         if (!interval.closed) {
            interval.overlap = true;
            interval.closed = true;
            this.count(interval);
         }
      }

      this.intervals.clear();
      this.owners.clear();
      this.current = null;
      ++this.epoch;
   }

   public void restore(Map<Integer, Stats> saved, long unknown, long failed) {
      this.endSession();
      this.statistics.clear();
      this.statistics.putAll(saved);
      this.unknown = unknown;
      this.failed = failed;
   }

   public void reset() {
      this.intervals.clear();
      this.owners.clear();
      this.statistics.clear();
      this.current = null;
      this.unknown = this.failed = 0L;
      ++this.epoch;
   }

   public static final class Stats {
      public long logs;
      public long acorns;
      public long decidable;
      public long successes;
      public long estimatedLogs;
      public long estimatedAcorns;
      public long multiAwardLogs;

      public double average() {
         return this.logs == 0L ? (double)0.0F : (double)this.acorns / (double)this.logs;
      }

      public double successRate() {
         return this.decidable == 0L ? Double.NaN : (double)100.0F * (double)this.successes / (double)this.decidable;
      }
   }

   private static final class Mine {
      final long position;
      final long start;
      final long baseline;
      long deadline;
      boolean confirmed;
      boolean failed;

      Mine(long position, long tick, long baseline, int wait) {
         this.position = position;
         this.start = tick;
         this.baseline = baseline;
         this.deadline = tick + (long)wait;
      }
   }

   private static final class Interval {
      final int fortune;
      final List<Mine> mines = new ArrayList();
      long acorns;
      long closedAt;
      boolean overlap;
      boolean closed;
      boolean invalidated;
      long countedLogs;
      long countedAcorns;
      long countedDecidable;
      long countedSuccesses;
      long countedMulti;

      Interval(int fortune) {
         this.fortune = fortune;
      }

      long deadline() {
         return this.mines.stream().mapToLong((m) -> m.deadline).max().orElse(0L);
      }
   }

   private static record Owner(Interval interval, Mine mine) {
   }

   public final class Observation implements LongConsumer {
      private final List<Interval> candidates;
      private final boolean ambiguous;

      private Observation(List<Interval> candidates, boolean ambiguous) {
         this.candidates = candidates;
         this.ambiguous = ambiguous;
      }

      public Observation asUnknown() {
         return TrackerEngine.this.new Observation(this.candidates, true);
      }

      public Observation combine(Observation next) {
         List<Interval> union = new ArrayList(this.candidates);

         for(Interval interval : next.candidates) {
            if (!union.contains(interval)) {
               union.add(interval);
            }
         }

         return TrackerEngine.this.new Observation(union, this.ambiguous || next.ambiguous || !this.candidates.equals(next.candidates));
      }

      public void accept(long amount) {
         if (amount > 0L) {
            if (!this.ambiguous && this.candidates.size() == 1 && !((Interval)this.candidates.getFirst()).invalidated) {
               Interval interval = (Interval)this.candidates.getFirst();
               if (interval.closed) {
                  TrackerEngine.this.uncount(interval);
               }

               interval.acorns += amount;
               if (interval.closed) {
                  TrackerEngine.this.count(interval);
               }
            } else {
               TrackerEngine var10000 = TrackerEngine.this;
               var10000.unknown += amount;

               for(Interval interval : this.candidates) {
                  if (interval.closed) {
                     TrackerEngine.this.uncount(interval);
                  }

                  interval.overlap = true;
                  if (interval.closed) {
                     TrackerEngine.this.count(interval);
                  }
               }
            }

            ++TrackerEngine.this.epoch;
         }
      }
   }
}
