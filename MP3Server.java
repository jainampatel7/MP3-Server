import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * A MP3 Server for sending mp3 files over a socket connection.
 */
public class MP3Server {
    public static void main(String[] args) {
        ServerSocket server;
        try {
            System.out.println("<Server on>");
            server = new ServerSocket(50000);
            while (true) {
                Socket client = server.accept();
                System.out.println("connected");
                ClientHandler handler = new ClientHandler(client);
                Thread thread = new Thread(handler);
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("<An unexpected exception occurred>");
            return;
        }
    }
}

/**
 * Class - ClientHandler
 *
 * This class implements Runnable, and will contain the logic for handling
 * responses and requests to and from a given client. The threads you create in
 * MP3Server will be constructed using instances of this class.
 */
final class ClientHandler implements Runnable {

    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    public ClientHandler(Socket clientSocket) {
        try {
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException i) {
            System.out.println(i);
        }
    }

    /**
     * This method is the start of execution for the thread. See the handout for
     * more details on what to do here.
     */
    public void run() {
        int songSize = -1;
        String songN = "";
        String ArtN = "";
        boolean Song;
        while (true){
            SongHeaderMessage  header = null;
            try {
                if ((header = (SongHeaderMessage) inputStream.readObject())!= null){
                    Song = header.isSongHeader();
                    if (header.isSongHeader()){
                        songSize = header.getFileSize();
                        songN = header.getSongName();
                        ArtN = header.getArtistName();

                    }
                    break;
                }

            } catch (ClassNotFoundException c){

            } catch (IOException i){

            }
        }
        String file = request.getArtistName() + " - " + request.getSongName() + ".mp3";
        if (fileInRecord(fileN)){
            outputStream.flush();
            sendByteArray(byte[])
        }



        // if ((request = (SongRequest) inputStream.readObject()) != null) {
//                if (request.isDownloadRequest()) {
//
//                    if (fileInRecord(file)) {
//                        File songfile = new File(file);
//                        int fileSize = songfile.length() > Integer.MAX_VALUE ? Integer.MAX_VALUE
//                                : (int) songfile.length();
//                        for (int i = 0; i < fileSize; i += 1000) {
//                            byte[] bit = new byte[fileSize - i >= 1000 ? 1000 : fileSize - i];
//
//                        }
//                    }
//                }
    }


    /**
     * Searches the record file for the given filename.
     *
     * @param fileName
     *            the fileName to search for in the record file
     * @return true if the fileName is present in the record file, false if the
     *         fileName is not
     */
    private static boolean fileInRecord(String fileName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.indexOf(fileName) != -1) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            System.out.println("<An unexpected exception occurred>");
            System.out.printf("<Exception message: %s>\n", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Read the bytes of a file with the given name into a byte array.
     *
     * @param fileName
     *            the name of the file to read
     * @return the byte array containing all bytes of the file, or null if an error
     *         occurred
     */
    private static byte[] readSongData(String fileName) {
        try {
            File file = new File(fileName);
            FileInputStream fis = new FileInputStream(file);
            int bytes = (int) file.length();
            byte[] songData = new byte[bytes];
            fis.read(songData);

            return songData;
        } catch (IOException e) {
            System.out.println("<An unexpected exception occurred>");
            System.out.printf("<Exception message: %s>\n", e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Split the given byte array into smaller arrays of size 1000, and send the
     * smaller arrays to the client using SongDataMessages.
     *
     * @param songData
     *            the byte array to send to the client
     */
    private void sendByteArray(byte[] songData) {
        try {
            for (int i = 0; i < songData.length / 1000; i++) {
                if (i == songData.length / 1000) {
                    byte[] songBytes = new byte[songData.length % 1000];
                    for (int j = 0; j < songData.length % 1000; j++) {
                        songBytes[j] = songData[i * 1000 + j];
                    } // end if
                    outputStream.writeObject(songBytes);
                    return;
                } // end if

                byte[] songBytes = new byte[1000];
                for (int j = 0; j < 1000; j++) {
                    songBytes[j] = songData[i * 1000 + j];
                }
                outputStream.writeObject(songBytes);

            } // end for
        } catch (IOException e) {
            System.out.println("<An unexpected exception occurred>");
            System.out.printf("<Exception message: %s>\n", e.getMessage());
            e.printStackTrace();
            return;
        } // end try-catch
    }

    /**
     * Read ''record.txt'' line by line again, this time formatting each line in a
     * readable format, and sending it to the client. Send a ''null'' value to the
     * client when done, to signal to the client that you've finished sending the
     * record data.
     */
    private void sendRecordData() {
        try {
            PrintWriter pw = new PrintWriter(outputStream, true);
            try {
                BufferedReader br = new BufferedReader(new FileReader("record.txt"));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] lineParse = line.split(".", 2);
                    String fileName = lineParse[0];
                    String[] songAndArtistNames = fileName.split(" - ", 2);
                    String artistName = songAndArtistNames[0];
                    String songName = songAndArtistNames[1];
                    String formatLine = String.format("\"%s\" by: %s", songName, artistName);

                    pw.println(formatLine);
                }
                pw.println((String) null);

            } catch (FileNotFoundException e) {
                pw.print((String) null);
            } // end try-catch
        } catch (IOException e) {
            System.out.println("<An unexpected exception occurred>");
            System.out.printf("<Exception message: %s>\n", e.getMessage());
            e.printStackTrace();
            return;
        }
    }
}
