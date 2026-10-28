package endfield.desktop;

import arc.util.Log;
import org.jetbrains.annotations.TestOnly;

import java.util.HashSet;
import java.util.Set;

import static endfield.desktop.Demodulator.openModule;

@TestOnly
public class Test2 {
	public static Set<String> getPackages(Class<?> clazz) {
		return clazz.getModule().getPackages();
	}

	public static Set<Module> getJvmModules() {
		return ModuleLayer.boot().modules();
	}

	public static Set<String> getJvmPackages() {
		HashSet<String> packages = new HashSet<>();

		for (Module module : ModuleLayer.boot().modules()) {
			packages.addAll(module.getPackages());
		}

		return packages;
	}

	public static void openAll() {
		try {
			Module em = Test2.class.getModule();

			for (Module module : ModuleLayer.boot().modules()) {
				for (String pkg : module.getPackages()) {
					openModule(module, pkg, em);
				}
			}
		} catch (Throwable e) {
			Log.err(e);
		}
	}
}
