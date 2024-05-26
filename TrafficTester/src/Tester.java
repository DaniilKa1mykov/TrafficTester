import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class Tester {

    public static void main(String[] args) {


        boolean generatorFlag = args[0].equals("-g");
        if (generatorFlag) {
            System.out.println("Введите количество пакетов");
            Scanner in = new Scanner(System.in);
            int NumberOfPackets = in.nextInt();
            System.out.println("Введите объем пакета");
            int PacketSize = in.nextInt();
            System.out.println("Введите частоту отправки");
            int rate = in.nextInt();
            generator(NumberOfPackets, PacketSize, rate);
        } else {
            client();
        }
    }

    public static void generator(int NumberOfPackets, int PacketSize, int rate) {

        try (
                Socket tcpSocket = new Socket("localhost", 8081);
                PrintWriter out =
                        new PrintWriter(tcpSocket.getOutputStream(), true);
                BufferedReader in =
                        new BufferedReader(
                                new InputStreamReader(tcpSocket.getInputStream()))
        ) {
            out.println(NumberOfPackets);
            out.println(System.nanoTime());
            for (int j = 0; j < NumberOfPackets; j += rate) {
                for (int i = 0; i < rate; i++) {
                    byte[] dataChunk = new byte[PacketSize];
                    Arrays.fill(dataChunk, (byte) 0);
                    out.println(Arrays.toString(dataChunk));
                    in.readLine();
                }
            }
            System.out.print("Послано " + NumberOfPackets + " пакетов объемом " + PacketSize + " байт с частотой " + rate + " пак/раз\n");
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to localhost");
            System.exit(1);
        }
    }

    public static void client() {
        int NumberOfReceivedPackets = 0;
        long startSendingTime = 0;
        long startRecievingTime = 0;
        int NumberOfPacketsSent = 0;
        try (
                ServerSocket serverSocket =
                        new ServerSocket(8081);
                Socket clientSocket = serverSocket.accept();
                PrintWriter out =
                        new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()))
        ) {
            String inputLine;
            boolean firstTime = true;
            boolean secondTime = true;
            boolean thirdTime = true;
            while ((inputLine = in.readLine()) != null) {
                if (firstTime) {
                    NumberOfPacketsSent = Integer.parseInt(inputLine);
                    firstTime = false;
                } else if (secondTime) {
                    startSendingTime = Long.parseLong(inputLine);
                    secondTime = false;
                } else {
                    if (thirdTime) {
                        startRecievingTime = System.nanoTime();
                        thirdTime = false;
                    }
                    NumberOfReceivedPackets += 1;
                    out.println(inputLine);
                    System.out.println(inputLine);
                }
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port 8081 or listening for a connection");
            System.out.println(e.getMessage());
        } finally {
            long endRecievingtime = System.nanoTime();
            int lostPackets = NumberOfPacketsSent - NumberOfReceivedPackets;
            long receivingTime = endRecievingtime - startRecievingTime;
            long rtt = endRecievingtime - startSendingTime;
            System.out.print("Количество принятых пакетов: " + NumberOfReceivedPackets + "\nЗадержка: " + Math.round(rtt / Math.pow(10, 6)) + " мс" + "\nСкорость принятия пакетов: " + Math.round(NumberOfReceivedPackets / (receivingTime / Math.pow(10, 6))) + " пак/мс" + "\nУтеряно пакетов: " + lostPackets + "\n");
        }
    }
}