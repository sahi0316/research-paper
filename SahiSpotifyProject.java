package data.tracks;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.hc.core5.http.ParseException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.neovisionaries.i18n.CountryCode;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.miscellaneous.AudioAnalysis;
import se.michaelthelin.spotify.model_objects.miscellaneous.AudioAnalysisTrack;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetAudioAnalysisForTrackRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetAudioFeaturesForSeveralTracksRequest;

public class SahiSpotifyProject {
	private static final String FILE_NAME = "src/test/resources/SpotifyFeatures.xlsx";
	 
	private static final String clientId = "8d59008787624c91a1e433c1a8e94c2d";
	private static final String clientSecret = "eabe9b4f1e8e47ff83a497af8840c21b";

	private static final SpotifyApi spotifyApi = new SpotifyApi.Builder().setClientId(clientId)
			.setClientSecret(clientSecret).build();

	private static final ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();

	public static void main(String[] args) {
		authenticate();
		List<Song> songList = readSongList();
		songList.stream().forEach(song -> song.setTrackId(searchTracks(song)));
		List<SongDetails> audioFeatures = songList.stream()
				.filter(song -> song.getTrackId() != null || song.getTrackId().length() > 0)
				.map(song -> getAudioFeatures(song.getTrackId(), song))
				.filter(sd -> sd != null)
				.collect(Collectors.toList());

		//System.out.println(audioFeatures);
		writeToExcel(audioFeatures);
	}

	private static void writeToExcel(List<SongDetails> songDetailsList) {
		    XSSFWorkbook workbook = new XSSFWorkbook();
	        XSSFSheet sheet = workbook.createSheet("AudioFeatures");
	        
	        int rowNum = 0;
	        System.out.println("Creating excel");
	       
	        Row row = sheet.createRow(rowNum++);
	        int colNum = 0;
	        writeToHeaderCell(sheet, "Song Name", row, colNum++);
	        writeToHeaderCell(sheet, "Artist", row, colNum++);
	        writeToHeaderCell(sheet, "Track ID", row, colNum++);
	        writeToHeaderCell(sheet, "Duration in ms", row, colNum++);
	        writeToHeaderCell(sheet, "Acousticness", row, colNum++);
	        writeToHeaderCell(sheet, "Danceability", row, colNum++);
	        writeToHeaderCell(sheet, "Energy", row, colNum++);
	        writeToHeaderCell(sheet, "Instrumentalness", row, colNum++);
	        writeToHeaderCell(sheet, "Liveness", row, colNum++);
	        writeToHeaderCell(sheet, "Loudness", row, colNum++);
	        writeToHeaderCell(sheet, "Mode", row, colNum++);
	        writeToHeaderCell(sheet, "Speechiness", row, colNum++);
	        writeToHeaderCell(sheet, "Tempo", row, colNum++);
	        writeToHeaderCell(sheet, "TimeSignature", row, colNum++);
	        writeToHeaderCell(sheet, "type", row, colNum++);
	        writeToHeaderCell(sheet, "Valence", row, colNum++);
	        writeToHeaderCell(sheet, "Key", row, colNum++);
	        
	        //writeToHeaderCell(sheet, "CodeString", row, colNum++);
	        //writeToHeaderCell(sheet, "Duration", row, colNum++);
	        	
	        for (SongDetails sd : songDetailsList) {
	        	row = sheet.createRow(rowNum++);
	        	AudioFeatures af = sd.getAudioFeature();
	        	//AudioAnalysisTrack at = sd.getAudioAnalysisTrack();
	        	
	        	colNum = 0;
	        	writeToCell(sheet, sd.getSong().getName(), row, colNum++);
	        	writeToCell(sheet, sd.getSong().getArtist(), row, colNum++);
	        	writeToCell(sheet, sd.getSong().getTrackId(), row, colNum++);
	        	writeToCell(sheet, af.getDurationMs().toString(), row, colNum++);
	        	writeToCell(sheet, af.getAcousticness().toString(), row, colNum++);
	        	writeToCell(sheet, af.getDanceability().toString(), row, colNum++);
	        	writeToCell(sheet, af.getEnergy().toString(), row, colNum++);
	        	writeToCell(sheet, af.getInstrumentalness().toString(), row, colNum++);
	        	writeToCell(sheet, af.getLiveness().toString(), row, colNum++);
	        	writeToCell(sheet, af.getLoudness().toString(), row, colNum++);
	        	writeToCell(sheet, af.getMode().name()+"-"+Integer.valueOf(af.getMode().mode).toString(), row, colNum++);
	        	writeToCell(sheet, af.getSpeechiness().toString(), row, colNum++);
	        	writeToCell(sheet, af.getTempo().toString(), row, colNum++);
	        	writeToCell(sheet, af.getTimeSignature().toString(), row, colNum++);
	        	writeToCell(sheet, af.getType().type, row, colNum++);
	        	writeToCell(sheet, af.getValence().toString(), row, colNum++);
	        	writeToCell(sheet, af.getKey().toString(), row, colNum++);
	        	//writeToCell(sheet, at.getCodeString(), row, colNum++);
	        	//writeToCell(sheet, at.getDuration().toString(), row, colNum++);
	        }

	        try {
	            FileOutputStream outputStream = new FileOutputStream(FILE_NAME);
	            workbook.write(outputStream);
	            workbook.close();
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	        System.out.println("Done");
	}
	
	private static void writeToHeaderCell(XSSFSheet sheet, String value, Row row, int colNum) {
		CellStyle headerCellStyle = sheet.getWorkbook().createCellStyle();
		headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.index);
		headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		row.setRowStyle(headerCellStyle);
		
		Cell cell = row.createCell(colNum);
		cell.setCellValue(value);
		sheet.autoSizeColumn(colNum);
	}

	private static void writeToCell(XSSFSheet sheet, String value, Row row, int colNum) {
		Cell cell1 = row.createCell(colNum);
		cell1.setCellValue(value);
		sheet.autoSizeColumn(colNum);
	}

	public static List<Song> readSongList() {
		List<String> list = new ArrayList<>();
		try (BufferedReader br = Files.newBufferedReader(Paths.get("src/main/resources/song-names.txt"))) {
			list = br.lines().collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return list.stream().map(str -> {
			String[] lines = str.trim().split(",");
			Song song = new Song();
			song.setName(lines[0].trim());
			song.setArtist(lines[1].trim());
			return song;
		}).collect(Collectors.toList());

	}

	public static String searchTracks(Song song) {
		String ids = "";
		try {
			SearchTracksRequest searchTracksRequest = spotifyApi.searchTracks(song.getName()).market(CountryCode.IN)
					.limit(1).build();
			final Paging<Track> trackPaging = searchTracksRequest.execute();
			List<Track> items = Arrays.asList(trackPaging.getItems());
			ids = items.stream().filter(track -> searchArtists(track.getArtists(), song.getArtist(), song.getName()))
					.map(track -> track.getId()).collect(Collectors.joining(","));
			// items.stream().forEach(track -> System.out.println(track.toString()));
			// System.out.println("Total: " + trackPaging.getTotal());
		} catch (IOException | SpotifyWebApiException | ParseException e) {
			System.out.println("Error: " + e.getMessage());
		}

		return ids;
	}

	private static boolean searchArtists(ArtistSimplified[] artists, String artist, String songName) {
		List<ArtistSimplified> spArtists = Arrays.asList(artists);
		boolean found = spArtists.stream().anyMatch(art -> art.getName().equals(artist));
		if (!found) {
			System.out.println("song: " + songName + " artist:" + artist);
			return false;
		}

		return found;
	}

	public static SongDetails getAudioFeatures(String id, Song song)  {
		
		AudioFeatures[] audioFeatures = null;
		SongDetails songDetails = new SongDetails();
		try {
			
			if (id != null && id.length() > 0) {
				Thread.sleep(1000);
				String[] ids = { id };
				GetAudioFeaturesForSeveralTracksRequest getAudioFeaturesForSeveralTracksRequest = spotifyApi
						.getAudioFeaturesForSeveralTracks(ids).build();

				audioFeatures = getAudioFeaturesForSeveralTracksRequest.execute();
				System.out.println("Length: " + audioFeatures.length);
				//AudioAnalysisTrack audioAnalysisTrack = getAudioAnalysisForTrack(id);
				
				
				songDetails.setSong(song);
				songDetails.setAudioFeature(audioFeatures[0]);
				//if(audioAnalysisTrack != null) {
				//	songDetails.setAudioAnalysisTrack(audioAnalysisTrack);	
				//}
				
				return songDetails;
			}

		} catch (IOException | SpotifyWebApiException | ParseException | InterruptedException e) {
			System.out.println("Error: " + e.getMessage());
		}

		return null;
	}
	
	
	public static AudioAnalysisTrack getAudioAnalysisForTrack(String id) {
		AudioAnalysisTrack audioAnalysisTrack = null;
		try {
			GetAudioAnalysisForTrackRequest getAudioAnalysisForTrackRequest = spotifyApi
					.getAudioAnalysisForTrack(id)
					.build();
			final AudioAnalysis audioAnalysis = getAudioAnalysisForTrackRequest.execute();
			System.out.println("Track duration: " + audioAnalysis.getTrack().getDuration());
			audioAnalysisTrack =  audioAnalysis.getTrack();
		} catch (IOException | SpotifyWebApiException | ParseException e) {
			System.out.println("Error: " + e.getMessage());
		}

		return audioAnalysisTrack;
	}

	public static void authenticate() {

		try {
			final ClientCredentials clientCredentials = clientCredentialsRequest.execute();
			// Set access token for further "spotifyApi" object usage
			spotifyApi.setAccessToken(clientCredentials.getAccessToken());

			System.out.println("Expires in: " + clientCredentials.getExpiresIn());
		} catch (IOException | SpotifyWebApiException | ParseException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
}

class Song {
	private String name;
	private String artist;
	private String trackId;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getTrackId() {
		return trackId;
	}

	public void setTrackId(String trackId) {
		this.trackId = trackId;
	}

}

class SongDetails {
	private Song song;
	private AudioFeatures audioFeature;
	private AudioAnalysisTrack audioAnalysisTrack;
	
	public Song getSong() {
		return song;
	}
	public void setSong(Song song) {
		this.song = song;
	}
	public AudioFeatures getAudioFeature() {
		return audioFeature;
	}
	public void setAudioFeature(AudioFeatures audioFeature) {
		this.audioFeature = audioFeature;
	}
	public AudioAnalysisTrack getAudioAnalysisTrack() {
		return audioAnalysisTrack;
	}
	public void setAudioAnalysisTrack(AudioAnalysisTrack audioAnalysisTrack) {
		this.audioAnalysisTrack = audioAnalysisTrack;
	}
	
	
}
