package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user =  new User(name,mobile);
        users.add(user);
//        userPlaylistMap.put(user,new ArrayList<Playlist>());
        System.out.println(user.getName());
        System.out.println(user.getMobile());
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Artist artist = null;
        for(Artist artistss: artists){
            if(artistss.getName().equals(artistName)){
                artist = artistss;
            }
        }

        Album album = new Album(title);

        // if artist not exist create new Artist
        if(artist == null){
            artist = createArtist(artistName);
        }

        List<Album> albumList = new ArrayList<>();
        if(artistAlbumMap.containsKey(artist)){
            albumList = artistAlbumMap.get(artist);
        }
        albumList.add(album);
        artistAlbumMap.put(artist,albumList);


        albums.add(album);

        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        Album album = null;
        for(Album check : albums){
            if(check.getTitle().equals(albumName)){
                album = check;
            }
        }

        if(album == null)throw new Exception("User does not exist");

        Song song = new Song(title,length);
            song.setLikes(0);
            songs.add(song);

            //update albumSongMap
            List<Song> songList = new ArrayList<>();
            if(albumSongMap.containsKey(album)) songList = albumSongMap.get(album);
            songList.add(song);
            albumSongMap.put(album,songList);

            return song;

    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = null;
        for(User usr : users){
            if(usr.getMobile().equals(mobile))user = usr;
        }

        if(user == null)throw new Exception("User does not exist");
        else {
            // create playlist
            Playlist playlist = createPlayList(title);


            // update playlistSongMap list of all songs having length
            List<Song> songList = new ArrayList<>();
            for(Song song : songs){
                if(song.getLength() == length)songList.add(song);
            }
            playlistSongMap.put(playlist,songList);

            // update listener list
            updateListener(user,playlist);


            // update creatorPlaylistMap
            creatorPlaylistMap.put(user,playlist);

            // update userPlaylistMap
            updateUserPlayList(user,playlist);


            return playlist;
        }
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = null;
        for(User usr : users){
            if(usr.getMobile().equals(mobile))user = usr;
        }

        if(user == null)throw new Exception("User does not exist");
        else {
            // create playlist
            Playlist playlist = createPlayList(title);

            // update playlistSongMap list of all songs present in songTitles
            List<Song> songList = new ArrayList<>();
            for(String songTitle : songTitles){
                for(Song song : songs){
                    if(song.getTitle().equals(songTitle))songList.add(song);
                }
            }
            playlistSongMap.put(playlist,songList);

            // update listener list
            updateListener(user,playlist);

            // update creatorPlaylistMap
            creatorPlaylistMap.put(user,playlist);

            // update userPlaylistMap
            updateUserPlayList(user,playlist);

            return playlist;
        }
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = null;
        for(User currUser : users){
            if(currUser.getMobile().equals(mobile))user = currUser;
        }
        // user does not exist
        if(user == null)throw new Exception("User does not exist");

        Playlist playlist = null;
        for(Playlist currPlaylist: playlists){
            if(currPlaylist.getTitle().equals(playlistTitle))playlist = currPlaylist;
        }
        // playlist does not exist
        if(playlist == null)throw new Exception("Playlist does not exist");

        // if user is creator of playlist do nothing and return playlist
        if(creatorPlaylistMap.containsKey(user)){
            if(creatorPlaylistMap.get(user).getTitle().equals(playlistTitle))return playlist;
        }

        // if user is already present in listener list do nothing and return playlist
        List<User> listenerList = new ArrayList<>();
        if(playlistListenerMap.containsKey(playlist)){
            listenerList = playlistListenerMap.get(playlist);
            for(User user1 : listenerList){
                if(user1.getMobile().equals(mobile)) return playlist;
            }
        }
        // update listener map
        listenerList.add(user);
        playlistListenerMap.put(playlist,listenerList);
        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = null;
        for(User currUser : users){
            if(currUser.getMobile().equals(mobile))user = currUser;
        }
        // user does not exist
        if(user == null)throw new Exception("User does not exist");

        Song song = null;
        for(Song currSong: songs){
            if(currSong.getTitle().equals(songTitle))song = currSong;
        }
        // playlist does not exist
        if(song == null)throw new Exception("Song does not exist");

        // if user has already liked song
        List<User> songLikeUser = new ArrayList<>();
                if(songLikeMap.containsKey(song)) songLikeUser = songLikeMap.get(song);
        for(User userLike : songLikeUser){
            if(userLike.getMobile().equals(mobile)) return song;
        }

        // update song like
        song.setLikes(song.getLikes()+1);
        songLikeUser.add(user);
        songLikeMap.put(song,songLikeUser);

        // update like of artist
        updateArtistLike(song);

        return song;
    }

    public String mostPopularArtist() {
        int like = -1;
        String popularArtist = "";
        for (Artist artist: artists){
            if(like < artist.getLikes()){
                like = artist.getLikes();
                popularArtist = artist.getName();
            }
        }
        return popularArtist;
    }

    public String mostPopularSong() {
        int like = -1;
        String popularSong = "";
        for (Song song: songs){
            if(like < song.getLikes()){
                like = song.getLikes();
                popularSong = song.getTitle();
            }
        }
        return popularSong;
    }

    public Playlist createPlayList(String title){
        Playlist playlist = new Playlist(title);
        playlists.add(playlist);
        return playlist;
    }
    public void updateListener(User user,Playlist playlist){
        List<User> listenerList = new ArrayList<>();
        if(playlistListenerMap.containsKey(playlist)) listenerList= playlistListenerMap.get(playlist);
        listenerList.add(user);
        playlistListenerMap.put(playlist,listenerList);
    }

    public void updateUserPlayList(User user, Playlist playlist){
        List<Playlist> playlistList = new ArrayList<>();
        if(userPlaylistMap.containsKey(user)){
            playlistList = userPlaylistMap.get(user);
        }
        playlistList.add(playlist);
        userPlaylistMap.put(user,playlistList);
    }
    public void updateArtistLike(Song likedSong){
        int like = -1;
        String popularArtist = "";
        for(Artist artist: artists){
            if(artistAlbumMap.containsKey(artist)){
                List<Album> albumList= artistAlbumMap.get(artist);
                for (Album album : albumList){
                    if(albumSongMap.containsKey(album)){
                        List<Song> songList = albumSongMap.get(album);
                        for (Song song : songList){
                            if(song.equals(likedSong)){
                                artist.setLikes(artist.getLikes()+1);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}
