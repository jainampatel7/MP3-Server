import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * An MP3 Client to request .mp3 files from a server and receive them over the
 * socket connection.
 */
public class MP3Client {
    public static void main(String[] args) {
        Socket serverConnection = null;
        Scanner inUser = new Scanner(System.in);
        Scanner inServer = null;
        ObjectOutputStream outputStream;
        String response;

        try {
            serverConnection = new Socket("localhost", 50000);
            inServer = new Scanner(serverConnection.getInputStream());
            outputStream = new ObjectOutputStream(serverConnection.getOutputStream());
            System.out.println("<Connected to the server>");
        } catch (IOException e) {
            System.out.println("<An unexpected exception occurred>");
            System.out.printf("<Exception message: %s>\n", e.getMessage());

            if (inServer != null) {
                inServer.close();
            }
            if (serverConnection != null) {
                try {
                    serverConnection.close();
                } catch (IOException i) {
                    i.printStackTrace();
                }
            }
            return;
        }


        System.out.print("Do you want to see list of songs, or just download a song? ");
        String request = inUser.nextLine();
        try {
            while (!request.toLowerCase().equals("exit")) {
                if (request.toLowerCase().equals("download")) {
                    System.out.print("What is the title of the song? ");
                    String name = inUser.nextLine();
                    System.out.print("What is the artist of the song? ");
                    String artist = inUser.nextLine();
                    SongRequest song = new SongRequest(true, name, artist);
                    outputStream.writeObject(song);
                } else if (request.toLowerCase().equals("list")) {
                    SongRequest song = new SongRequest(false, null, null);
                    outputStream.writeObject(song);
                } else {
                    System.out.print("Do you want to see list of songs, or just download a song? ");
                    request = inUser.nextLine();
                    continue;
                }
                Thread client = new Thread(new ResponseListener(serverConnection));
                client.start();
                client.join();
                System.out.print("Do you want to see list of songs, or just download a song? ");
                request = inUser.nextLine();
            }

            inUser.close();
            inServer.close();
            serverConnection.close();
        } catch (IOException | InterruptedException e) {
            System.out.println("<An unexpected exception occurred>");
            System.out.printf("<Exception message: %s>\n", e.getMessage());
            e.printStackTrace();
        }
    }
}

/**
 * This class implements Runnable, and will contain the logic for listening for
 * server responses. The threads you create in MP3Server will be constructed
 * using instances of this class.
 */
final class ResponseListener implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream ois;

    public ResponseListener(Socket clientSocket) throws IllegalArgumentException {
        if (clientSocket == null) {
            throw new IllegalArgumentException("clientSocket argument is null");
        } else {
            try {
                this.clientSocket = clientSocket;
                ois = new ObjectInputStream(clientSocket.getInputStream());
            } catch (IOException e) {
                System.out.println("<An unexpected exception occurred>");
                System.out.printf("<Exception message: %s>\n", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Listens for a response from the server.
     * <p>
     * Continuously tries to read a SongHeaderMessage. Gets the artist name, song
     * name, and file size from that header, and if the file size is not -1, that
     * means the file exists. If the file does exist, the method then subsequently
     * waits for a series of SongDataMessages, takes the byte data from those data
     * messages and writes it into a properly named file.
     */
    public void run() {
        while (true) {
            Object object;
            Object data;
            SongHeaderMessage message;

            try {
                object = ois.readObject();
                if (object == null || !(object instanceof SongHeaderMessage)) {
                    continue;
                }

                message = (SongHeaderMessage) object;
                if (message.isSongHeader()) {
                    // download request, write song to file
                    String artist = message.getArtistName();
                    String song = message.getSongName();
                    int fileSize = message.getFileSize();
                    if (fileSize == -1) {
                        System.out.println("Song is unavailable.\n");
                        return;
                    }

                    String fileName = String.format("savedSongs\\%s - %s.mp3", artist, song);

                    while ((data = ois.readObject()) != null) {
                        if (!(data instanceof byte[])) {
                            return;
                        }

                        byte[] songBytes = (byte[]) data;
                        this.writeByteArrayToFile(songBytes, fileName);
                    } // end while
                    System.out.println("<File download successful>");
                    return;
                } else {
                    // not download request, print record (receive strings from server)
                    //BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    //while((data = br.readLine()) != null) {
                    while((data = ois.readObject()) != null) {
                        System.out.println(data);
                    } // end while
                    System.out.println("<End of record>");
                    return;
                } // end if-else
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("<An unexpected exception occurred>");
                System.out.printf("<Exception message: %s>\n", e.getMessage());
                e.printStackTrace();
                return;
            } // end try-catch-IOException|ClassNotFoundException

        } // end while
    }

    /**
     * Writes the given array of bytes to a file whose name is given by the fileName
     * argument.
     *
     * @param songBytes
     *            the byte array to be written
     * @param fileName
     *            the name of the file to which the bytes will be written
     */
    private void writeByteArrayToFile(byte[] songBytes, String fileName) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName, true);
            try {
                fos.write(songBytes);
            } catch (IOException e) {
                return;
            } // end try-catch-IOException
        } catch (FileNotFoundException e) {
            return;
        } // end try-catch-FileNotFoundException
    }
}