package com.eatsleepcode.mazebuilder;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockDoorWood;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.BlockFace;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import java.util.HashMap;
import java.util.Random;

public class MazeBuilder extends PluginBase implements Listener {
	private final HashMap<String, Boolean> inMaze = new HashMap<>();
	private Position entrance;
	private Position exit;
	private Block mazeMaterial;
	private Item prize;
	private final Random random = new Random();
	private static final String[] PRIZES = { "cookie", "deepslate_emerald_ore", "diamond_block", "netherite_block", "netherite_sword", "netherite_pickaxe" };

	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("maze")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(TextFormat.RED + "Only players can use this command.");
				return true;
			}

			if (args.length < 3) {
				sender.sendMessage(TextFormat.RED + "Usage: /maze <material> <width> <length> [prize]");
				return true;
			}

			Player player = (Player) sender;
			mazeMaterial = getMaterial(args[0]);
			int width = ensureOdd(Integer.parseInt(args[1]));
			int length = ensureOdd(Integer.parseInt(args[2]));
			prize = args.length > 3 ? getPrize(args[3]) : Item.get(Item.COOKIE);

			generateMaze(player, width, length);
			player.sendMessage(TextFormat.GREEN + "Maze generated!");
			return true;
		}
		return false;
	}

	private void generateMaze(Player player, int width, int length) {
		Level level = player.getLevel();
		Position playerPos = player.getPosition();
		int startX = playerPos.getFloorX();
		int startY = playerPos.getFloorY();
		int startZ = playerPos.getFloorZ();
		
		// Determine direction
		int dx = 0, dz = 0;
		BlockFace facing = player.getDirection();

		switch (facing) {
			case SOUTH: dz = 1; break;
			case WEST: dx = 1; break;
			case NORTH: dz = -1; break;
			case EAST: dx = -1; break;
			default: 
				dz = 1;
				break;
		}

		// Adjust entrance and exit
		entrance = new Position(startX, startY, startZ, level);
		exit = new Position(startX + dx * length, startY, startZ + dz * length, level);

		// Build maze boundaries
		for (int x = 0; x < width; x++) {
			for (int z = 0; z < length; z++) {
				int realX = startX + (x * dx) + (z * dz);
				int realZ = startZ + (x * dz) + (z * dx);

				// Floor
				level.setBlock(new Position(realX, startY, realZ, level), mazeMaterial, true, true);
				// Walls (2 blocks high)
				level.setBlock(new Position(realX, startY + 1, realZ, level), mazeMaterial, true, true);
				level.setBlock(new Position(realX, startY + 2, realZ, level), mazeMaterial, true, true);
			}
		}

		// Entrance and exit doors
		level.setBlock(entrance, new BlockDoorWood(), true, true);
		level.setBlock(exit, new BlockDoorWood(), true, true);

		// Generate maze paths
		createMazePaths(level, startX, startY, startZ, width, length);
	}

	private void createMazePaths(Level level, int startX, int startY, int startZ, int width, int length) {
		// Simple randomized path carving
		boolean[][] mazeGrid = new boolean[width][length];
		mazeGrid[1][1] = true;
		carvePaths(mazeGrid, 1, 1, width, length);

		for (int x = 1; x < width; x += 2) {
			for (int z = 1; z < length; z += 2) {
				if (mazeGrid[x][z]) {
					level.setBlock(new Position(startX + x, startY + 1, startZ + z, level), Block.get(Block.AIR), true, true);
					level.setBlock(new Position(startX + x, startY + 2, startZ + z, level), Block.get(Block.AIR), true, true);
				}
			}
		}
	}

	private void carvePaths(boolean[][] grid, int x, int z, int width, int length) {
		int[][] directions = { { 0, 2 }, { 2, 0 }, { 0, -2 }, { -2, 0 } };
		shuffleArray(directions);

		for (int[] dir : directions) {
			int nx = x + dir[0], nz = z + dir[1];
			if (nx > 0 && nz > 0 && nx < width - 1 && nz < length - 1 && !grid[nx][nz]) {
				grid[nx][nz] = true;
				grid[x + dir[0] / 2][z + dir[1] / 2] = true;
				carvePaths(grid, nx, nz, width, length);
			}
		}
	}

	private void shuffleArray(int[][] array) {
		for (int i = array.length - 1; i > 0; i--) {
			int j = random.nextInt(i + 1);
			int[] temp = array[i];
			array[i] = array[j];
			array[j] = temp;
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Position to = event.getTo();

		if (to.equals(entrance)) {
			inMaze.put(player.getName(), true);
		}

		if (inMaze.getOrDefault(player.getName(), false) && to.equals(exit)) {
			player.getInventory().addItem(prize);
			player.sendMessage(TextFormat.GOLD + "You completed the maze! Prize: " + prize.getName());
			inMaze.remove(player.getName());

			// Regenerate the maze
			generateMaze(player, random.nextInt(5) + 11, random.nextInt(5) + 11);
		}
	}

	private Block getMaterial(String name) {
		switch (name.toLowerCase()) {
			case "stone": return Block.get(Block.STONE);
			case "concrete": return Block.get(Block.CONCRETE);
			case "sandstone": return Block.get(Block.SANDSTONE);
			case "planks": return Block.get(Block.WOOD);
			case "snow": return Block.get(Block.SNOW_BLOCK);
			case "gravel": return Block.get(Block.GRAVEL);
			default: return Block.get(Block.OBSIDIAN);
		}
	}

	private Item getPrize(String name) {
		if (name.equalsIgnoreCase("random")) {
			name = PRIZES[random.nextInt(PRIZES.length)];
		}

		switch (name.toLowerCase()) {
			case "deepslate_emerald_ore": return Item.get(Item.DEEPSLATE_EMERALD_ORE);
			case "diamond_block": return Item.get(Item.DIAMOND_BLOCK);
			case "netherite_block": return Item.get(Item.NETHERITE_BLOCK);
			case "netherite_sword": return Item.get(Item.NETHERITE_SWORD);
			case "netherite_pickaxe": return Item.get(Item.NETHERITE_PICKAXE);
			default: return Item.get(Item.COOKIE);
		}
	}

	private int ensureOdd(int num) {
		return (num % 2 == 0) ? num - 1 : num;
	}
}
