package endfield.net;

import arc.util.io.Reads;
import arc.util.io.Writes;
import endfield.world.blocks.units.UnitAssembler2;
import mindustry.io.TypeIO;
import mindustry.net.Packet;
import mindustry.world.Tile;

public class AssemblerUnitSpawnedCallPacket2 extends Packet {
	private byte[] data;
	public Tile tile;

	public AssemblerUnitSpawnedCallPacket2() {
		data = NODATA;
	}

	@Override
	public void write(Writes write) {
		TypeIO.writeTile(write, tile);
	}

	@Override
	public void read(Reads read, int length) {
		data = read.b(length);
	}

	@Override
	public void handled() {
		BAIS.setBytes(data);
		tile = TypeIO.readTile(READ);
	}

	@Override
	public void handleClient() {
		UnitAssembler2.assemblerUnitSpawned(tile);
	}

	@Override
	public boolean allow(boolean server) {
		return !server;
	}
}
