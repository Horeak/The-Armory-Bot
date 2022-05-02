package Core.Commands.Voice.Objects;

public class TrackObject
{
	public PlayListInfo playlistInfo;
	public String loadType;
	public Track[] tracks;
	public TrackException exception;
	
	public boolean isPlayList(){
		return playlistInfo != null && playlistInfo.name != null;
	}
	
	public class PlayListInfo{
		public int selectedTrack;
		public String name;
	}
	
	public class Track{
		public String track;
		public TrackInfo info;
	}
	
	
	public class TrackInfo{
		public String identifier;
		public Boolean isSeekable;
		public String author;
		public Long length;
		public boolean isStream;
		public Long position;
		public String title;
		public String uri;
	}
	
	public class TrackException{
		public String message;
		public String severity;
	}
}
