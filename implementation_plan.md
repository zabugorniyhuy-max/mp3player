# 3D Positional Audio for Custom Jukebox Discs & UI Enhancements

This plan addresses three main requirements:
1. Make custom music discs play in 3D space from the Jukebox without overriding the player's personal UI music player.
2. Allow clearing/reverting custom music discs back to regular discs from the UI.
3. Add a "Favorites" tab to the main menu.

## User Review Required

> [!WARNING]
> Since we use raw javax.sound.sampled.Clip for playing MP3s dynamically (Minecraft's sound engine only supports static .ogg assets), the 3D audio will be a simulated distance-based volume fade (up to 64 blocks). Does this sound good?

## Proposed Changes

### Network & Backend (Server & Client)

#### [MODIFY] MP3PlayerMod.java
- Register a new ClearDiscPayload global receiver on the server.
- The handler will search the player's inventory for a custom disc (with CustomMp3 tag), remove the tag and custom name, and return it to a vanilla disc state.

#### [NEW] ClearDiscPayload.java (in 
etwork package)
- Simple empty payload used to trigger the clear disc action.

#### [MODIFY] MP3PlayerClient.java
- Update the PlayCustomDiscPayload receiver to NOT use MusicPlayer.getInstance(). Instead, it will spawn a JukeboxAudioPlayer instance for that specific BlockPos.
- Register a StopCustomDiscPayload receiver to stop the audio when the disc stops/ejects.
- Add a client tick event listener that iterates all active JukeboxAudioPlayer instances, calculates the distance from the player to the jukebox, and updates the volume dynamically.

#### [NEW] StopCustomDiscPayload.java (in 
etwork package)
- Payload containing a BlockPos to tell clients to stop the music playing at that jukebox.

### Audio Engine

#### [NEW] JukeboxAudioPlayer.java (in client/audio package)
- A specialized audio player class similar to MusicPlayer, but designed for 3D positional playback.
- It will hold the BlockPos, the Clip, and update its FloatControl.Type.MASTER_GAIN based on distance.

#### [MODIFY] JukeboxManagerMixin.java
- Add injection to stopPlaying to broadcast StopCustomDiscPayload to tracking players.

### UI Enhancements

#### [MODIFY] MainScreen.java
- Add a third tab "⭐" for Favorite tracks in the main menu.
- Create enderFavoritesTab that displays tracks from MusicPlayer.getInstance().getFavorites().
- Add an option to the track context menu: "Очистить пластинку" (Clear Disc) which sends ClearDiscPayload to the server.

## Verification Plan

### Automated Tests
- Build using gradlew clean build to verify compilation.

### Manual Verification
- Join a single-player or local server world.
- Burn a song to a disc and insert it into a Jukebox. Verify it plays in 3D space (getting quieter as you walk away, fading out at 64 blocks) and doesn't interrupt your UI music player.
- Eject the disc to verify the music stops.
- Test the "Clear Disc" button to revert the disc back to normal.
- Check the Favorites tab to see liked songs.