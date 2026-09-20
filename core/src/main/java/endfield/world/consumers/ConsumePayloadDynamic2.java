package endfield.world.consumers;

import arc.func.Func;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.type.PayloadSeq;
import mindustry.type.PayloadStack;
import mindustry.ui.ReqImage;
import mindustry.world.consumers.ConsumePayloadDynamic;
import mindustry.world.meta.StatValues;

import java.util.concurrent.atomic.AtomicReference;

public class ConsumePayloadDynamic2 extends ConsumePayloadDynamic {
	public <T extends Building> ConsumePayloadDynamic2(Func<T, Seq<PayloadStack>> payloads) {
		super(payloads);
	}

	@Override
	public float efficiency(Building build) {
		Seq<PayloadStack> pay = payloads.get(build);

		if (pay != null) {
			float multiple = multiplier.get(build);
			for (PayloadStack stack : pay) {
				if (!build.getPayloads().contains(stack.item, Math.round(stack.amount * multiple))) {
					return 0f;
				}
			}
		}
		return 1f;
	}

	@Override
	public void trigger(Building build) {
		Seq<PayloadStack> pay = payloads.get(build);

		if (pay != null) {
			float multiple = multiplier.get(build);
			for (PayloadStack stack : pay) {
				build.getPayloads().remove(stack.item, Math.round(stack.amount * multiple));
			}
		}
	}

	@Override
	public void build(Building build, Table table) {
		AtomicReference<Seq<?>> current = new AtomicReference<>(payloads.get(build));

		table.table(cont -> {
			table.update(() -> {
				Seq<PayloadStack> pay = payloads.get(build);

				if (current.get() != pay) {
					rebuild(build, cont);
					current.set(pay);
				}
			});

			rebuild(build, cont);
		});
	}

	public void rebuild(Building build, Table table) {
		PayloadSeq inv = build.getPayloads();
		Seq<PayloadStack> pay = payloads.get(build);

		if (pay != null) {
			table.clear();
			table.table(c -> {
				int i = 0;
				for (PayloadStack stack : pay) {
					c.add(new ReqImage(StatValues.stack(stack.item, Math.round(stack.amount * multiplier.get(build))),
							() -> inv.contains(stack.item, Math.round(stack.amount * multiplier.get(build))))).padRight(8);
					if (++i % 4 == 0) c.row();
				}
			}).left();
		}
	}
}
