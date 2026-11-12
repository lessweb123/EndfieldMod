package endfield.world.blocks.sandbox;

import arc.Core;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Icon;
import mindustry.gen.Unit;
import mindustry.net.Packets.AdminAction;
import mindustry.ui.Styles;
import mindustry.world.Block;

public class NextWave extends Block {
	public NextWave(String name) {
		super(name);

		update = true;
		solid = false;
		targetable = false;
		hasItems = false;
		configurable = true;
	}

	public class NextWaveBuild extends Building {
		@Override
		public void buildConfiguration(Table table) {
			table.button(Icon.upOpen, Styles.cleari, () -> configure(1)).size(50f).tooltip(Core.bundle.get("text.next-wave-1"));
			table.button(Icon.warningSmall, Styles.cleari, () -> configure(10)).size(50f).tooltip(Core.bundle.get("text.next-wave-10"));
		}

		@Override
		public void configured(Unit builder, Object value) {
			if (value instanceof Number index) {
				for (int i = index.intValue(); i > 0; i--) {
					if (Vars.net.client()) Call.adminRequest(Vars.player, AdminAction.wave, null);
					else Vars.logic.runWave();
				}
			}
		}
	}
}
