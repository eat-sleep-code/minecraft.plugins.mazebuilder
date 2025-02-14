# MazeBuilder Minecraft (Nukkit) Plugin

Build a lighted, randomly-generated maze.   Players successfully completing the maze will receive a prize.   After the maze has been successfully completed, a new maze is generated.  When a player enters the maze, they must successfully complete it to exit the maze.

## Prerequisites
- [Nukkit Minecraft Server](https://github.com/PetteriM1/NukkitPetteriM1Edition/releases)

## Installation 
- Place the `MazeBuilder.jar` file in the `<Nukkit Installation Folder>/plugins/` folder.

## Usage

- Create a stone-walled, maze that is ~50 blocks wide by ~100 blocks long. Reward successful completion with a random prize.

  `/maze stone 50 100 random`

## Known Issues

- Gravel, sand, or water found within the build area can potentially overwhelm the creation of the maze.   This would result in some debris that needs to be cleaned up. 

## Building Project

Run `mvn clean package`.   The output will be saved to `/target/MazeBuilder.jar`