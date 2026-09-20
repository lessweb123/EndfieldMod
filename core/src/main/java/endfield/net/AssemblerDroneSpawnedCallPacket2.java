package endfield.net;

import arc.util.io.Reads;
import arc.util.io.Writes;
import endfield.world.blocks.units.UnitAssembler2;
import mindustry.io.TypeIO;
import mindustry.net.Packet;
import mindustry.world.Tile;

public class AssemblerDroneSpawnedCallPacket2 extends Packet {
	private byte[] data;
	public Tile tile;
	public int id;

	public AssemblerDroneSpawnedCallPacket2() {
		data = NODATA;
	}

	@Override
	public void write(Writes write) {
		TypeIO.writeTile(write, tile);
		write.i(id);
	}

	@Override
	public void read(Reads read, int length) {
		data = read.b(length);
	}

	@Override
	public void handled() {
		BAIS.setBytes(data);
		tile = TypeIO.readTile(READ);
		id = READ.i();
	}

	@Override
	public void handleClient() {
		UnitAssembler2.assemblerDroneSpawned(tile, id);
	}

	@Override
	public boolean allow(boolean server) {
		return !server;
	}
}
