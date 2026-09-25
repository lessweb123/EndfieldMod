package endfield.core;

import arc.Core;
import arc.Events;
import arc.flabel.FLabel;
import arc.math.Mathf;
import arc.util.Align;
import arc.util.Log;
import arc.util.OS;
import arc.util.Strings;
import endfield.Vars2;
import endfield.audio.Musics2;
import endfield.audio.Sounds2;
import endfield.content.Blocks2;
import endfield.content.Bullets2;
import endfield.content.Items2;
import endfield.content.Liquids2;
import endfield.content.Loadouts2;
import endfield.content.Overrides;
import endfield.content.Planets2;
import endfield.content.SectorPresets2;
import endfield.content.StatusEffects2;
import endfield.content.TechTrees;
import endfield.content.UnitCommands2;
import endfield.content.UnitTypes2;
import endfield.content.Weathers2;
import endfield.game.Team2;
import endfield.graphics.AtlasPackHandle;
import endfield.graphics.CacheLayer2;
import endfield.graphics.MathRenderer;
import endfield.graphics.Pixmaps2;
import endfield.graphics.Regions2;
import endfield.graphics.ScreenSampler;
import endfield.graphics.Shaders2;
import endfield.graphics.Textures2;
import endfield.graphics.g2d.CutBatch;
import endfield.graphics.g2d.DevastationBatch;
import endfield.graphics.g2d.FragmentationBatch;
import endfield.graphics.g2d.RangeExtractor;
import endfield.graphics.g2d.VaporizeBatch;
import endfield.mod.AdaptiveCoreDatabase;
import endfield.mod.Mods2;
import endfield.net.Call2;
import endfield.ui.Elements;
import endfield.ui.Fonts2;
import endfield.ui.Icon2;
import endfield.ui.SplashDrawer;
import endfield.ui.Styles2;
import endfield.ui.Tex2;
import endfield.util.MockPlatformImpl;
import endfield.util.PlatformImpl;
import endfield.util.script.Scripts2;
import endfield.world.Worlds;
import endfield.world.patterns.PatternManager;
import mindustry.Vars;
import mindustry.game.EventType.AtlasPackEvent;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.game.EventType.DisposeEvent;
import mindustry.game.EventType.FileTreeInitEvent;
import mindustry.game.EventType.MusicRegisterEvent;
import mindustry.mod.Mod;
import mindustry.mod.Mods.LoadedMod;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.Setting;

import static endfield.Vars2.author;
import static endfield.Vars2.linkGitHub;
import static endfield.Vars2.modName;
import static endfield.Vars2.platformImpl;

/**
 * Main entry point of the mod. Handles startup things like content loading, entity registering, and utility
 * bindings.
 *
 * @author LessWeb
 * @see Vars2
 */
public final class EndFieldMod extends Mod {
	public static Mod mod;
	public static LoadedMod loadedMod;

	public static FloatingText floatingText;

	static {
		try {
			Class<?> impl = Class.forName(OS.isAndroid ?
					Core.app != null && Core.app.getVersion() >= 33 ?
							"endfield.android.AndroidImpl2" :
							"endfield.android.AndroidImpl" :
					"endfield.desktop.DesktopImpl");
			platformImpl = (PlatformImpl) impl.getConstructor().newInstance();
		} catch (Throwable e) {
			platformImpl = new MockPlatformImpl().setup();

			Log.err(e);
		}
	}

	public EndFieldMod() {
		mod = this;

		ClassMap2.load();

		Events.on(ClientLoadEvent.class, event -> {
			PatternManager.register();

			if (Vars.headless || Vars2.isPlugin || Core.settings.getBool("closed-dialog")) return;

			FLabel label = new FLabel(Core.bundle.format("text.author", author));
			BaseDialog dialog = new BaseDialog(Core.bundle.get("text.name"));
			dialog.buttons.button(Core.bundle.get("close"), dialog::hide).size(210f, 64f);
			dialog.buttons.button(Core.bundle.get("text.link-github"), () -> {
				if (!Core.app.openURI(linkGitHub)) {
					Vars.ui.showErrorMessage("@linkfail");
					Core.app.setClipboardText(linkGitHub);
				}
			}).size(210f, 64f);
			dialog.cont.pane(t -> {
				t.image(Core.atlas.find(modName + "-cover")).left().size(600f, 413f).pad(3f).row();
				t.add(Core.bundle.get("text.version")).left().growX().wrap().pad(4f).labelAlign(Align.left).row();
				t.add(label).left().row();
				t.add(Core.bundle.get("text.type")).left().growX().wrap().pad(4f).labelAlign(Align.left).row();
				t.add(Core.bundle.get("text.other-0")).left().growX().wrap().pad(4f).labelAlign(Align.left).row();
				t.add(Core.bundle.get("text.other-1")).left().growX().wrap().width(550f).maxWidth(600f).pad(4f).labelAlign(Align.left).row();
				t.add(Core.bundle.get("text.other-3")).left().growX().wrap().width(550f).maxWidth(600f).pad(4f).labelAlign(Align.left).row();
			}).grow().center().maxWidth(600f);
			dialog.show();
		});

		Events.on(FileTreeInitEvent.class, event -> {
			Core.app.post(() -> {
				loadedMod = Vars.mods.getMod(EndFieldMod.class);

				if (!Vars.headless && !Mods2.isEnabled("omaloon") && loadedMod != null && Core.settings.getBool("splash-drawer", false)) {
					try {
						SplashDrawer.add(loadedMod);
					} catch (Exception e) {
						Log.err(e);
					}
				}
			});

			if (!Vars.headless) {
				Fonts2.load();
				Sounds2.load();

				Core.app.post(() -> {
					Pixmaps2.load();
					Textures2.load();
					Regions2.load();
					Shaders2.load();
					CacheLayer2.load();
					MathRenderer.load();
					RangeExtractor.load();

					Tex2.init();

					Vars2.fragBatch = new FragmentationBatch();
					Vars2.cutBatch = new CutBatch();
					Vars2.vaporBatch = new VaporizeBatch();
					Vars2.devasBatch = new DevastationBatch();
				});
			}
		});

		Events.on(MusicRegisterEvent.class, event -> {
			if (!Vars.headless) {
				Musics2.load();
			}
		});

		Events.on(DisposeEvent.class, event -> {
			if (!Vars.headless) {
				Shaders2.dispose();
				ScreenSampler.dispose();
			}
		});

		Events.on(AtlasPackEvent.class, event -> {
			for (AtlasPackHandle handle : CacheLayer2.handles) handle.getPack(event.multiPacker);
		});

		// To prevent damage to other mod, it can only be enabled during testing
		if (!OS.isIos) {
			Core.app.post(Scripts2::init);
		}

		Vars2.listener = new EndFieldListener();
	}

	@Override
	public void loadContent() {
		if (Vars2.isPlugin) return;

		try {
			Regions2.addAll();

			Call2.init();

			Worlds.loadFallback();
			Worlds.load();

			UnitCommands2.load();

			Team2.load();
			Bullets2.load();
			Items2.load();
			StatusEffects2.load();
			Liquids2.load();
			UnitTypes2.load();
			Blocks2.loadInternal();
			Blocks2.load();
			Weathers2.load();
			Overrides.load();
			Planets2.load();
			SectorPresets2.load();
			TechTrees.load();
			Loadouts2.load();
		} catch (Throwable e) {
			Log.err("Loading content exception", e);
		}
	}

	@Override
	public void init() {
		Vars2.listener.updateInit();

		if (!Vars2.isPlugin) {
			try {
				StatusEffects2.init();
				UnitTypes2.init();
				Overrides.init();
			} catch (Throwable e) {
				Log.err("Initialization content exception", e);
			}
		}

		if (!Vars.headless) {
			Icon2.load();

			Tex2.load();
			Styles2.load();
			Elements.load();

			ScreenSampler.setup();
		}

		if (Vars.ui != null) {
			if (Vars.ui.settings != null) {
				//add endfield settings
				Vars.ui.settings.addCategory(Core.bundle.get("text.settings"), Icon2.reactionIcon, table -> {
					table.checkPref("closed-dialog", false);
					table.checkPref("floating-text", true);
					table.checkPref("splash-drawer", false);
					table.checkPref("animated-shields", true);
					table.checkPref("tesla-range", true);
					table.sliderPref("vaporize-batch", 300, 0, 1000, 1, s -> Strings.autoFixed(s, 2));
					table.pref(new Setting(Core.bundle.get("text.game-data")) {
						@Override
						public void add(SettingsTable table) {
							table.button(name, Elements.gameDataDialog::show).margin(14).width(200f).pad(6);
							table.row();
						}
					});
					table.pref(new Setting(Core.bundle.get("text.export-data")) {
						@Override
						public void add(SettingsTable table) {
							table.button(name, Worlds::exportBlockData).margin(14).width(200f).pad(6);
							table.row();
						}
					});
				});
			}

			if (!Vars.headless && !Mods2.isEnabled("extra-utilities") && !Mods2.isX() && Core.settings.getBool("floating-text")) {
				String[] massages = Core.bundle.get("text.random-massage").split("@");

				floatingText = new FloatingText(massages[Mathf.random(massages.length - 1)]);
				floatingText.build(Vars.ui.menuGroup);
			}
		}

		AdaptiveCoreDatabase.init();
	}
}
