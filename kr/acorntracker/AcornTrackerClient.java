package kr.acorntracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.JsonOps;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1735;
import net.minecraft.class_1743;
import net.minecraft.class_1799;
import net.minecraft.class_1893;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_2649;
import net.minecraft.class_2653;
import net.minecraft.class_2680;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_6880;
import net.minecraft.class_7923;
import net.minecraft.class_9304;
import net.minecraft.class_9334;
import net.minecraft.class_9834;
import net.minecraft.class_9835;
import net.minecraft.class_304.class_11900;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AcornTrackerClient implements ClientModInitializer {
   private static final long ACORN_UNIT_PRICE = 5000L;
   public static final AcornTrackerClient INSTANCE = new AcornTrackerClient();
   private static final Gson JSON = (new GsonBuilder()).setPrettyPrinting().create();
   private static final Logger LOGGER = LoggerFactory.getLogger("acorn_tracker");
   private final Path directory = FabricLoader.getInstance().getConfigDir().resolve("acorn-tracker");
   private final TrackerEngine engine = new TrackerEngine();
   private final InventoryLedger inventory;
   private TrackerConfig config;
   private class_1799 exemplar;
   private long ticks;
   private long savedEpoch;
   private long suspendUntil;
   private int fortune;
   private boolean connected;
   private boolean invalidSave;
   private String scope;
   private Object world;
   private Object trackedPlayer;
   private class_304 settingsKey;

   public AcornTrackerClient() {
      TrackerEngine var10004 = this.engine;
      Objects.requireNonNull(var10004);
      this.inventory = new InventoryLedger(41, var10004::inventoryContext);
      this.config = new TrackerConfig();
      this.exemplar = class_1799.field_8037;
      this.savedEpoch = -1L;
   }

   public void onInitializeClient() {
      INSTANCE.initialize();
   }

   private void initialize() {
      try {
         Files.createDirectories(this.directory);
         if (Files.exists(this.directory.resolve("config.json"), new LinkOption[0])) {
            this.config = (TrackerConfig)JSON.fromJson(Files.readString(this.directory.resolve("config.json")), TrackerConfig.class);
            if (this.config == null) {
               this.config = new TrackerConfig();
            }
         }
      } catch (Exception e) {
         LOGGER.error("Cannot read Acorn Tracker config; tracking disabled until item registration", e);
      }

      try {
         this.config.useBundledAcornIfMissing();
      } catch (Exception e) {
         LOGGER.error("Cannot load the default acorn; manual item registration is required", e);
      }

      this.configure();
      this.settingsKey = KeyBindingHelper.registerKeyBinding(new class_304("key.acorn_tracker.settings", 79, class_11900.field_62556));
      ClientTickEvents.END_CLIENT_TICK.register(this::tick);
      ClientPlayConnectionEvents.JOIN.register((ClientPlayConnectionEvents.Join)(handler, sender, client) -> this.join(client));
      ClientPlayConnectionEvents.DISCONNECT.register((ClientPlayConnectionEvents.Disconnect)(handler, client) -> {
         this.inventory.settle(this.ticks + 2L);
         this.engine.endSession();
         this.saveStats();
         this.connected = false;
         this.inventory.clear();
         this.exemplar = class_1799.field_8037;
         this.world = null;
         this.trackedPlayer = null;
      });
      HudRenderCallback.EVENT.register((HudRenderCallback)(context, counter) -> this.render(context));
      ClientCommandRegistrationCallback.EVENT.register((ClientCommandRegistrationCallback)(dispatcher, access) -> {
         dispatcher.register(this.commands(false));
         dispatcher.register(this.commands(true));
      });
   }

   private LiteralArgumentBuilder<FabricClientCommandSource> commands(boolean korean) {
      LiteralArgumentBuilder<FabricClientCommandSource> hudCommand = (LiteralArgumentBuilder)ClientCommandManager.literal(korean ? "화면" : "hud").executes((c) -> {
         this.openSettings();
         return 1;
      });
      hudCommand.then(ClientCommandManager.literal(korean ? "전환" : "toggle").executes((c) -> {
         this.config.hud = !this.config.hud;
         this.saveConfig();
         return this.message("HUD 표시: " + (this.config.hud ? "켜짐" : "꺼짐"));
      }));
      if (korean) {
         hudCommand.then(ClientCommandManager.literal("켜기").executes((c) -> this.setHud(true)));
         hudCommand.then(ClientCommandManager.literal("끄기").executes((c) -> this.setHud(false)));
      }

      for(TrackerConfig.HudField field : TrackerConfig.HudField.values()) {
         hudCommand.then(this.booleanCommand(korean ? field.koreanCommand() : field.key, korean, (visible) -> this.setHudField(field, visible)).executes((c) -> this.setHudField(field, !this.config.visible(field))));
      }

      LiteralArgumentBuilder<FabricClientCommandSource> treesCommand = (LiteralArgumentBuilder)ClientCommandManager.literal(korean ? "나무" : "trees").executes((c) -> {
         this.client().method_63588(() -> this.client().method_1507(new HudSettingsScreen((class_437)null, 2)));
         return 1;
      });

      for(TreeType tree : TreeType.values()) {
         treesCommand.then(this.booleanCommand(korean ? tree.label.replace(" ", "") : tree.key, korean, (enabled) -> this.setTree(tree, enabled)).executes((c) -> this.setTree(tree, !this.config.measures(tree))));
      }

      String ticksArgument = korean ? "틱" : "ticks";
      return (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommandManager.literal(korean ? "도토리" : "acorn").executes((c) -> {
         this.openSettings();
         return 1;
      })).then(ClientCommandManager.literal(korean ? "설정" : "settings").executes((c) -> {
         this.openSettings();
         return 1;
      }))).then(ClientCommandManager.literal(korean ? "아이템" : "item").executes((c) -> this.registerItem()))).then(ClientCommandManager.literal(korean ? "대기" : "wait").then(ClientCommandManager.argument(ticksArgument, IntegerArgumentType.integer(1, 1200)).executes((c) -> {
         this.inventory.settle(this.ticks + 2L);
         this.engine.endSession();
         this.config.waitTicks = IntegerArgumentType.getInteger(c, ticksArgument);
         this.configure();
         this.saveConfig();
         this.saveStats();
         return this.message("판정 대기: " + this.config.waitTicks + "틱");
      })))).then(this.booleanCommand(korean ? "최대하나" : "maxone", korean, (enabled) -> {
         this.config.maxOneConfirmed = enabled;
         this.saveConfig();
         return this.message("원목당 최대 1개 조건: " + (enabled ? "켜짐" : "꺼짐"));
      }))).then(hudCommand)).then(treesCommand)).then(ClientCommandManager.literal(korean ? "통계" : "stats").executes((c) -> this.printStats()))).then(ClientCommandManager.literal(korean ? "초기화" : "reset").executes((c) -> {
         this.inventory.clear();
         this.engine.reset();
         this.invalidSave = false;
         this.saveStats();
         return this.message("현재 서버·캐릭터 통계를 초기화했습니다. 다음 인벤토리 동기화부터 측정합니다.");
      }));
   }

   private LiteralArgumentBuilder<FabricClientCommandSource> booleanCommand(String name, boolean korean, Function<Boolean, Integer> action) {
      LiteralArgumentBuilder<FabricClientCommandSource> command = ClientCommandManager.literal(name);
      if (korean) {
         command.then(ClientCommandManager.literal("켜기").executes((c) -> (Integer)action.apply(true)));
         command.then(ClientCommandManager.literal("끄기").executes((c) -> (Integer)action.apply(false)));
      } else {
         command.then(ClientCommandManager.argument("value", BoolArgumentType.bool()).executes((c) -> (Integer)action.apply(BoolArgumentType.getBool(c, "value"))));
      }

      return command;
   }

   private int setHud(boolean visible) {
      this.config.hud = visible;
      this.saveConfig();
      return this.message("HUD 표시: " + (visible ? "켜짐" : "꺼짐"));
   }

   private void configure() {
      this.config.hudScale = Double.isFinite(this.config.hudScale) ? Math.clamp(this.config.hudScale, (double)0.5F, (double)2.0F) : (double)1.0F;
      this.config.hudOpacity = Math.clamp((long)this.config.hudOpacity, 0, 100);
      this.config.waitTicks = Math.clamp((long)this.config.waitTicks, 1, 1200);
      this.config.confirmationTicks = Math.max(this.config.waitTicks, this.config.confirmationTicks);
      this.config.rollbackTicks = Math.max(this.config.waitTicks, this.config.rollbackTicks);
      this.engine.configure(this.config.waitTicks, this.config.confirmationTicks, this.config.rollbackTicks);
   }

   private int setHudField(TrackerConfig.HudField field, boolean visible) {
      this.config.setVisible(field, visible);
      this.saveConfig();
      return this.message(field.label + " 표시: " + (visible ? "켜짐" : "꺼짐"));
   }

   private class_310 client() {
      return class_310.method_1551();
   }

   public TrackerConfig config() {
      return this.config;
   }

   public String settingsKeyName() {
      return this.settingsKey.method_16007().getString();
   }

   public void openKeySettings(class_437 parent) {
      this.client().method_1507(new SettingsKeyScreen(parent));
   }

   public void setSettingsKey(class_3675.class_306 key) {
      this.settingsKey.method_1422(key);
      class_304.method_1426();
      this.settingsKey.method_23481(false);

      while(this.settingsKey.method_1436()) {
      }

      this.client().field_1690.method_1640();
   }

   public void persistHud() {
      this.saveConfig();
   }

   private int setTree(TreeType tree, boolean enabled) {
      this.config.setMeasured(tree, enabled);
      this.saveConfig();
      return this.message(tree.label + " 측정: " + (enabled ? "켜짐" : "꺼짐"));
   }

   public void openSettings() {
      this.client().method_63588(() -> this.client().method_1507(new HudSettingsScreen((class_437)null)));
   }

   private void join(class_310 client) {
      this.connected = true;
      this.world = client.field_1687;
      this.trackedPlayer = client.field_1724;
      this.inventory.clear();
      this.engine.reset();
      String server = client.method_1558() != null ? client.method_1558().field_3761 : (client.method_1576() != null ? "singleplayer:" + client.method_1576().method_27728().method_150() : "local");

      try {
         String key = server + ":" + String.valueOf(client.method_1548().method_44717());
         this.scope = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8)));
      } catch (Exception e) {
         throw new IllegalStateException(e);
      }

      this.loadStats();
      this.loadExemplar();
      this.fortune = this.fortune();
   }

   private void loadExemplar() {
      this.exemplar = class_1799.field_8037;
      if (this.config.acornStack != null && this.client().field_1687 != null) {
         this.exemplar = (class_1799)class_1799.field_24671.parse(this.client().field_1687.method_30349().method_57093(JsonOps.INSTANCE), this.config.acornStack).result().orElse(class_1799.field_8037);
      }
   }

   private int registerItem() {
      if (this.client().field_1724 != null && !this.client().field_1724.method_6047().method_7960()) {
         class_1799 held = this.client().field_1724.method_6047().method_46651(1);
         if (!this.exemplar.method_7960() && !class_1799.method_31577(this.exemplar, held) && this.engine.statistics().values().stream().anyMatch((s) -> s.logs > 0L || s.acorns > 0L)) {
            return this.message("다른 아이템으로 변경하려면 /도토리 초기화 후 등록해 주세요.");
         } else {
            this.inventory.settle(this.ticks + 2L);
            this.engine.endSession();
            this.saveStats();
            this.exemplar = held;
            this.config.acornStack = (JsonElement)class_1799.field_24671.encodeStart(this.client().field_1687.method_30349().method_57093(JsonOps.INSTANCE), this.exemplar).result().orElseThrow();
            this.saveConfig();
            this.saveStats();
            this.seedFromPlayer();
            return this.message("도토리 등록: " + this.exemplar.method_7964().getString() + " (아이템과 전체 구성 요소 일치)");
         }
      } else {
         return this.message("등록할 도토리를 주 손에 들어 주세요.");
      }
   }

   private void tick(class_310 client) {
      while(this.settingsKey.method_1436()) {
         if (client.field_1755 == null) {
            client.method_1507(new HudSettingsScreen((class_437)null));
         }
      }

      if (this.connected && client.field_1724 != null && client.field_1687 != null) {
         ++this.ticks;
         if (this.world != client.field_1687 || this.trackedPlayer != client.field_1724) {
            this.inventory.settle(this.ticks + 2L);
            this.engine.endSession();
            this.saveStats();
            this.inventory.clear();
            this.world = client.field_1687;
            this.trackedPlayer = client.field_1724;
            this.loadExemplar();
         }

         int level = this.fortune();
         if (level != this.fortune) {
            this.engine.fortuneChanged();
            this.fortune = level;
         }

         boolean suspended = client.field_1755 instanceof class_465 || this.ticks <= this.suspendUntil;
         if (suspended) {
            this.engine.measurementInterrupted();
         }

         this.inventory.suspend(suspended);
         this.inventory.settle(this.ticks);
         this.engine.advance();
         if (this.ticks % 100L == 0L && this.savedEpoch != this.engine.epoch()) {
            this.saveStats();
         }

      }
   }

   public int fortune() {
      if (this.client().field_1724 == null) {
         return 0;
      } else {
         class_1799 tool = this.client().field_1724.method_6047();
         if (!(tool.method_7909() instanceof class_1743)) {
            return 0;
         } else {
            class_9304 enchantments = (class_9304)tool.method_58695(class_9334.field_49633, class_9304.field_49385);
            return enchantments.method_57539().stream().filter((e) -> ((class_6880)e.getKey()).method_40225(class_1893.field_9130)).mapToInt((e) -> e.getIntValue()).findFirst().orElse(0);
         }
      }
   }

   public void beforeBreak(class_2338 pos) {
      if (this.connected && !this.invalidSave && !this.exemplar.method_7960() && this.inventory.ready() && this.client().field_1687 != null && this.config.measures(this.treeType(this.client().field_1687.method_8320(pos)))) {
         this.engine.attempt(pos.method_10063(), this.fortune(), this.inventory.total());
      }
   }

   public void breakResult(class_2338 pos, boolean success) {
      if (!success) {
         this.engine.localFailure(pos.method_10063());
      }

   }

   public void serverBlock(class_2338 pos, class_2680 state) {
      if (this.connected) {
         this.engine.serverBlock(pos.method_10063(), this.treeType(state) != null, state.method_26215());
      }

   }

   private TreeType treeType(class_2680 state) {
      return TreeType.fromBlockId(class_7923.field_41175.method_10221(state.method_26204()).toString());
   }

   public void inventoryInteraction() {
      this.suspendUntil = this.ticks + 4L;
      this.inventory.suspend(true);
      this.engine.measurementInterrupted();
   }

   private int count(class_1799 stack) {
      return !this.exemplar.method_7960() && class_1799.method_31577(stack, this.exemplar) ? stack.method_7947() : 0;
   }

   private long inventoryAcorns() {
      if (this.client().field_1724 == null) {
         return 0L;
      } else {
         long total = 0L;
         class_1661 playerInventory = this.client().field_1724.method_31548();

         for(int i = 0; i < playerInventory.method_5439(); ++i) {
            total += (long)this.count(playerInventory.method_5438(i));
         }

         return total + (long)this.count(this.client().field_1724.field_7512.method_34255());
      }
   }

   private void seedFromPlayer() {
      if (this.client().field_1724 != null) {
         int[] counts = new int[41];

         for(int i = 0; i < counts.length; ++i) {
            counts[i] = this.count(this.client().field_1724.method_31548().method_5438(i));
         }

         this.inventory.seed(counts, this.count(this.client().field_1724.field_7512.method_34255()));
      }
   }

   private class_1703 handler(int syncId) {
      if (this.client().field_1724 == null) {
         return null;
      } else if (syncId == 0) {
         return this.client().field_1724.field_7498;
      } else {
         return this.client().field_1724.field_7512.field_7763 == syncId ? this.client().field_1724.field_7512 : null;
      }
   }

   private void slot(class_1703 handler, int slotId, class_1799 stack) {
      if (handler != null && slotId >= 0 && slotId < handler.field_7761.size()) {
         class_1735 slot = (class_1735)handler.field_7761.get(slotId);
         if (slot.field_7871 == this.client().field_1724.method_31548()) {
            this.inventory.slot(slot.method_34266(), this.count(stack), this.ticks);
         }

      }
   }

   public void onInventory(class_2649 packet) {
      if (this.connected && !this.exemplar.method_7960()) {
         class_1703 handler = this.handler(packet.comp_3837());
         if (handler != null) {
            if (!this.inventory.ready()) {
               this.seedFromPlayer();
            } else {
               for(int i = 0; i < packet.comp_3839().size(); ++i) {
                  this.slot(handler, i, (class_1799)packet.comp_3839().get(i));
               }

               this.inventory.cursor(this.count(packet.comp_3840()), this.ticks);
            }
         }
      }
   }

   public void onSlot(class_2653 packet) {
      if (this.connected) {
         if (!this.inventory.ready()) {
            this.seedFromPlayer();
         } else {
            this.slot(this.handler(packet.method_11452()), packet.method_11450(), packet.method_11449());
         }
      }
   }

   public void onPlayerSlot(class_9835 packet) {
      if (this.connected) {
         if (!this.inventory.ready()) {
            this.seedFromPlayer();
         } else {
            this.inventory.slot(packet.comp_2891(), this.count(packet.comp_2892()), this.ticks);
         }
      }
   }

   public void onCursor(class_9834 packet) {
      this.inventory.cursor(this.count(packet.comp_2890()), this.ticks);
   }

   private int message(String text) {
      if (this.client().field_1724 != null) {
         this.client().field_1724.method_7353(class_2561.method_43470("[Acorn] " + text), false);
      }

      return 1;
   }

   private int printStats() {
      this.engine.statistics().forEach((level, s) -> this.message(String.format(Locale.ROOT, "행운 %d: 원목 %,d / 도토리 %,d / 평균 획득량 %.2f%% / 성공률 %s (판정 %,d, 성공 %,d) / 추정 원목 %,d, 도토리 %,d", level, s.logs, s.acorns, (double)100.0F * s.average(), Double.isNaN(s.successRate()) ? "—" : String.format(Locale.ROOT, "%.2f%%", s.successRate()), s.decidable, s.successes, s.estimatedLogs, s.estimatedAcorns)));
      long var10001 = this.engine.unknown();
      return this.message("출처 불명 " + var10001 + " / 실패·미확인 " + this.engine.failed() + " / 대기 " + this.engine.pending());
   }

   private void render(class_332 ctx) {
      if (this.connected && this.config.hud && !this.client().field_1690.field_1842 && this.client().field_1724 != null && !(this.client().field_1755 instanceof HudSettingsScreen) && !(this.client().field_1755 instanceof HudPositionScreen)) {
         HudRenderer.render(ctx, this.config, this.hudRows(false), false);
      }
   }

   public List<HudRenderer.Row> hudRows(boolean preview) {
      TrackerEngine.Stats s = (TrackerEngine.Stats)this.engine.statistics().getOrDefault(this.fortune, new TrackerEngine.Stats());
      boolean sample = preview && s.logs == 0L;
      if (sample) {
         s = new TrackerEngine.Stats();
         s.logs = 21600L;
         s.acorns = 1245L;
         s.decidable = 800L;
         s.successes = 46L;
         s.estimatedLogs = 20800L;
      }

      List<HudRenderer.Row> rows = new ArrayList();
      if (this.config.visible(TrackerConfig.HudField.TITLE)) {
         rows.add(new HudRenderer.Row("도토리", "", HudRenderer.Kind.TITLE));
      }

      if (this.config.visible(TrackerConfig.HudField.LOGS)) {
         rows.add(new HudRenderer.Row("원목", String.format(Locale.ROOT, "%,d개", s.logs), HudRenderer.Kind.NORMAL));
      }

      if (this.config.visible(TrackerConfig.HudField.ACORNS)) {
         rows.add(new HudRenderer.Row("도토리", String.format(Locale.ROOT, "%,d개", s.acorns), HudRenderer.Kind.ACCENT));
      }

      if (this.config.visible(TrackerConfig.HudField.VALUE)) {
         boolean registered = !this.exemplar.method_7960() && this.client().field_1724 != null;
         long held = registered ? this.inventoryAcorns() : (preview ? 64L : 0L);
         String price = !registered && !preview ? "도토리 등록 필요" : String.format(Locale.ROOT, "%,d원", held * 5000L);
         rows.add(new HudRenderer.Row("도토리 총가격", price, HudRenderer.Kind.VALUE));
      }

      if (this.config.visible(TrackerConfig.HudField.FORTUNE)) {
         rows.add(new HudRenderer.Row("행운", String.valueOf(sample ? 17 : this.fortune), HudRenderer.Kind.NORMAL));
      }

      if (this.config.visible(TrackerConfig.HudField.AVERAGE)) {
         rows.add(new HudRenderer.Row("평균 획득량", String.format(Locale.ROOT, "%.2f%%", (double)100.0F * s.average()), HudRenderer.Kind.AVERAGE));
      }

      if (this.config.visible(TrackerConfig.HudField.ESTIMATED) && s.estimatedLogs > 0L) {
         rows.add(new HudRenderer.Row("추정 구간 포함", String.format(Locale.ROOT, "%,d개", s.estimatedLogs), HudRenderer.Kind.DETAIL));
      }

      if (this.config.visible(TrackerConfig.HudField.WARNINGS) && this.config.maxOneConfirmed && (s.multiAwardLogs > 0L || s.acorns > s.logs)) {
         rows.add(new HudRenderer.Row("원목당 최대 1개 조건 위반", "", HudRenderer.Kind.WARNING));
      }

      if (this.invalidSave) {
         rows.add(new HudRenderer.Row("통계 파일 오류: 저장 중지", "", HudRenderer.Kind.WARNING));
      }

      return rows;
   }

   private void loadStats() {
      this.invalidSave = false;
      Path path = this.directory.resolve(this.scope + ".json");

      try {
         if (Files.exists(path, new LinkOption[0])) {
            Saved saved = (Saved)JSON.fromJson(Files.readString(path), Saved.class);
            if (saved == null || saved.statistics == null) {
               throw new IllegalStateException("Invalid statistics file");
            }

            this.engine.restore(saved.statistics, saved.unknown, saved.failed);
            if (saved.acornStack != null) {
               this.config.acornStack = saved.acornStack;
            }
         }
      } catch (Exception e) {
         this.invalidSave = true;
         LOGGER.error("Cannot load statistics; preserving original file", e);
      }

      this.savedEpoch = this.engine.epoch();
   }

   private void saveStats() {
      if (this.scope != null && !this.invalidSave) {
         Saved saved = new Saved();
         saved.statistics.putAll(this.engine.statistics());
         saved.unknown = this.engine.unknown();
         saved.failed = this.engine.failed();
         saved.acornStack = this.config.acornStack;
         if (this.write(this.directory.resolve(this.scope + ".json"), saved)) {
            this.savedEpoch = this.engine.epoch();
         }

      }
   }

   private void saveConfig() {
      this.write(this.directory.resolve("config.json"), this.config);
   }

   private boolean write(Path path, Object data) {
      try {
         Files.createDirectories(this.directory);
         Path temp = path.resolveSibling(String.valueOf(path.getFileName()) + ".tmp");
         Files.writeString(temp, JSON.toJson(data), StandardCharsets.UTF_8);

         try {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (AtomicMoveNotSupportedException var5) {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
         }

         return true;
      } catch (Exception e) {
         LOGGER.error("Cannot save {}", path, e);
         this.message("파일 저장 실패: " + String.valueOf(path.getFileName()));
         return false;
      }
   }

   private static final class Saved {
      Map<Integer, TrackerEngine.Stats> statistics = new TreeMap();
      JsonElement acornStack;
      long unknown;
      long failed;
   }
}
