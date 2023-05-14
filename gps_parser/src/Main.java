import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        String fileName = "gps2.txt";
        File file = new File(fileName);
        Scanner scanner = new Scanner(file);
        int lines = 0;
        try (LineNumberReader lnr = new LineNumberReader(new FileReader(file))) {
            while (lnr.readLine() != null) ;
            lines = lnr.getLineNumber();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Wczytano plik: "+fileName);
        String[] Dataset = createScannerDatasetWithoutValidLines(scanner, lines);
        handleSequences(Dataset);
    }
    private static void handleSequences(String[] Dataset) {
        for (int i = 0; i<Dataset.length; i++){
            if (Dataset[i] != null) {
                String standard = getStandardSequences(Dataset[i]);
                if (standard.equals("GPGGA")) {
                    System.out.println("\n\nSekwencja GGA (Fix information)");
                    GGASequence(Dataset[i]);
                }
                if (standard.equals("GPGSA")) {
                    System.out.println("\n\nSekwencja GSA (Overall Satellite data)");
                    GSASequence(Dataset[i]);
                }
                if (standard.equals("GPGSV")) {
                    System.out.println("\n\nSekwencja GSV (Detail Satellite data)");
                    GSVSequence(Dataset[i]);
                }
                if (standard.equals("GPRMC")) {
                    System.out.println("\n\nSekwencja RMC (Recommended minimum of data)");
                    RMCSequence(Dataset[i]);
                }
                if (standard.equals("GPGLL")) {
                    System.out.println("\n\nSekwencja GLL (Lat/Lon data)");
                    GLLSequence(Dataset[i]);
                }
                if (standard.equals("GPVTG")) {
                    System.out.println("\n\nSekwencja VTG (Vector track an Speed over the Ground)");
                    VTGSequence(Dataset[i]);
                }
            }
        }
    }
    private static String[] createScannerDatasetWithoutValidLines(Scanner scanner, int lines) {
        String[] Dataset = new String[lines];
        for (int i = 0; i<lines; i++) {
            String sentence = "";
            if (scanner.hasNextLine()) {
                sentence = scanner.nextLine();
            }
            ///odrzucenie linijki blednej
            //każda linia musi zaczynać się znakiem dolara ($) i kończyć znakiem nowej linii (\r)
            //długość linii nie może przekraczać 80 znaków (plus znak początku linii i znak nowej linii)
            //dane wysyłane przez odbiornik GPS są w danej linii podzielone znakami przecinka (,)
            System.out.print("\n"+(i+1)+". ");
            if (sentence.isEmpty() || sentence.charAt(0) != '$' || sentence.charAt(sentence.length() - 1) == '$' || sentence.length() > 82 || CheckIfFalseSum(sentence)){
                System.out.print("niepoprawny ");
                if (scanner.hasNextLine()) {
                    sentence = scanner.nextLine();
                }
                continue;
            }
            else {
                //gsv musi miec 19 przecinków inaczej odrzuca linijkę
                String standard = getStandardSequences(sentence);
                if (standard.equals("GPGSV")) {
                    if (countOccurrences(sentence, ',') != 19) {
                        System.out.print("niepoprawny ");
                        if (scanner.hasNextLine()) {
                            sentence = scanner.nextLine();
                        }
                        continue;
                    }
                }
            }
            Dataset[i] = sentence;
            String st = getStandardSequences(Dataset[i]);
            System.out.print("standard: "+st+" -> "+Dataset[i]);
        }
        return Dataset;
    }
    private static int countOccurrences(String zdanie, char s) {
        int count = 0;
        for (int i = 0; i < zdanie.length(); i++) {
            if (zdanie.charAt(i) == s) {
                count++;
            }
        }
        return count;
    }
    private static boolean CheckIfFalseSum(String zdanie) {
        String kontrolna = zdanie.substring(1, zdanie.length() - 3);
        String kontrolnaZdanie = kontrolna.substring(kontrolna.length() - 3, kontrolna.length() - 1);
        int suma_kontrolna = 0;
        for (int j = 0; j<kontrolna.length(); j++){
            suma_kontrolna ^= kontrolna.charAt(j);
        }
        String string_suma_kontrolna = Integer.toHexString(suma_kontrolna).toUpperCase();
        if (string_suma_kontrolna.equals(kontrolnaZdanie))
            return true;
        else
            return false;
    }
    private static void VTGSequence(String s) {
        String line = s;
        System.out.println("Określa aktualną pozycję (długość i szerokość geograficzną).");
        System.out.println(line);
        System.out.println("gdzie:");
        System.out.println("\t"+getStandardSequences(s).substring(2,5)+" - nagłówek");
        List<String> segments = splitString(line);
        System.out.println("\t"+segments.get(1)+","+segments.get(2)+" - ścieżka poruszania się (w stopniach)");
        System.out.println("\t"+segments.get(3)+","+segments.get(4)+" -  ścieżka poruszania się na podstawie współrzędnych magnetycznych – w stopniach");
        System.out.println("\t"+segments.get(5)+","+segments.get(6)+" - prędkość w węzłach");
        System.out.println("\t"+segments.get(7)+","+segments.get(8)+" - prędkość w km/h");
        System.out.println("\t"+segments.get(9)+" - suma kontrolna");
    }
    private static void GLLSequence(String s) {
        String line = s;
        System.out.println("Określa aktualną pozycję (długość i szerokość geograficzną).");
        System.out.println(line);
        System.out.println("gdzie:");
        System.out.println("\t"+getStandardSequences(s).substring(2,5)+" - nagłówek");
        List<String> segments = splitString(line);
        System.out.println("\t"+segments.get(1)+","+segments.get(2)+" - szerokość geograficzna (latitude) - "+segments.get(1).substring(0,2)+" deg "+segments.get(1).substring(2,8)+"' "+segments.get(2));
        System.out.println("\t"+segments.get(3)+","+segments.get(4)+" - długość geograficzna (longitude) - "+segments.get(3).substring(0,3)+" deg "+segments.get(3).substring(3,9)+"' "+segments.get(4));
        String timeUTC = segments.get(5);
        System.out.println("\t"+timeUTC+" - godzina ustalenia pozycji ["+timeUTC.substring(0,2)+":"+timeUTC.substring(2,4)+":"+timeUTC.substring(4,6)+"]");
        System.out.println("\t"+segments.get(6)+" - status (A – aktywny, V – nieaktywny) ");
        System.out.println("\t*"+segments.get(7)+" - suma kontrolna");
    }
    private static void RMCSequence(String s) {
        String line = s;
        System.out.println("Określane przez standard NMEA minimum danych, które musi wysyłać każdy odbiornik GPS. Interpretacja tych danych również pozwala określić pozycję GPS urządzenia.");
        System.out.println(line);
        System.out.println("gdzie:");
        System.out.println("\t"+getStandardSequences(s).substring(2,5)+" - nagłówek");
        List<String> segments = splitString(line);
        String timeUTC = segments.get(1);
        System.out.println("\t"+timeUTC+" - aktualność danych- "+timeUTC.substring(0,2)+":"+timeUTC.substring(2,4)+":"+timeUTC.substring(4,6));
        System.out.println("\t"+segments.get(2)+" - status (A – aktywny, V – nieaktywny)");
        System.out.println("\t"+segments.get(3)+","+segments.get(4)+" - szerokość geograficzna (latitude) - "+segments.get(3).substring(0,2)+" deg "+segments.get(3).substring(2,8)+"' "+segments.get(4));
        System.out.println("\t"+segments.get(5)+","+segments.get(6)+" - długość geograficzna (longitude) - "+segments.get(5).substring(0,2)+" deg "+segments.get(5).substring(2,9)+"' "+segments.get(6));
        System.out.println("\t"+segments.get(7)+" - prędkość obiektu (liczona w węzłach)");
        System.out.println("\t"+segments.get(8)+" - kąt śledzenia/poruszania się obiektu (w stopniach) – przydatny w celu określenia kierunku poruszania się obiektu, jeżeli urządzenie GPS nie jest wyposażone w kompas");
        String dateString = segments.get(9);
        System.out.println("\t"+dateString+" - data ("+dateString.substring(0,2)+" "+ dateString.substring(2,4)+" "+dateString.substring(4,6)+")");
        System.out.println("\t"+segments.get(10)+","+segments.get(11)+" - odchylenie magnetyczne ziemi");
        System.out.println("\t*"+segments.get(12)+" - suma kontrolna");
    }
    private static void GSVSequence(String s) {
        String line = s;
        System.out.println("Dostarcza dokładnych informacji o każdym widocznym satelicie (osobno)");
        System.out.println(line);
        System.out.println("gdzie:");
        System.out.println("\t"+getStandardSequences(s).substring(2,5)+" - nagłówek");
        List<String> segments = splitString(line);
        System.out.println("\t"+segments.get(1)+" - liczba linii, które powinna aplikacja odczytać w celu posiadania pełnych danych o wszystkich satelitach (wynika z ograniczenia do 80 znaków na linie NMEA)");
        System.out.println("\t"+segments.get(2)+" - aktualny numer linii (odnosi się do powyższego punktu)");
        System.out.println("\t"+segments.get(3)+" - liczba aktualnie widocznych satelitów");
        System.out.println("\t"+segments.get(4)+" - identyfikator PRN satelity");
        System.out.println("\t\t"+segments.get(5)+" - wyniesienie satelity nad poziom równika (stopnie)");
        System.out.println("\t\t"+segments.get(6)+" - azymut satelity (w stopniach)");
        System.out.println("\t\t"+segments.get(7)+" - SNR (signal to noise ratio) – poziom odbieranego sygnału");
        System.out.println("\t"+segments.get(8)+" - id satelity");
        System.out.println("\t\t"+segments.get(9)+" - wyniesienie na równik (w stopniach)");
        System.out.println("\t\t"+segments.get(10)+" - Azymut satelity (w stopniach)");
        System.out.println("\t\t"+segments.get(11)+" - Stosunek sygnał/szum (SNR) satelity");
        System.out.println("\t"+segments.get(12)+" - id satelity");
        System.out.println("\t\t"+segments.get(13)+" - wyniesienie na równik (w stopniach)");
        System.out.println("\t\t"+segments.get(14)+" - Azymut satelity (w stopniach)");
        System.out.println("\t\t"+segments.get(15)+" - Stosunek sygnał/szum (SNR) satelity");
        System.out.println("\t"+segments.get(16)+" - id satelity");
        System.out.println("\t\t"+segments.get(17)+" - wyniesienie na równik (w stopniach)");
        System.out.println("\t\t"+segments.get(18)+" - Azymut satelity (w stopniach)");
        System.out.println("\t\t"+segments.get(19)+" - Stosunek sygnał/szum (SNR) satelity");
        System.out.println("\t*"+segments.get(20)+" - suma kontrolna");

    }
    private static void GSASequence(String s) {
        String line = s;
        System.out.println("Dostarcza informacji o satelitach.");
        System.out.println(line);
        System.out.println("gdzie:");
        System.out.println("\t"+getStandardSequences(s).substring(2,5)+" - nagłówek");
        List<String> segments = splitString(line);
        System.out.println("\t"+segments.get(1)+","+segments.get(2)+" - automatyczny wybór pozycji (2D lub 3D) (M – manualny), pozycja 3D; możliwe wartości:");
        System.out.println("\t\t1 - brak ustalonej pozycji z tego satelity");
        System.out.println("\t\t2 - pozycja 2D");
        System.out.println("\t\t3 - pozycja 3D");
        System.out.print("\t");
        for (int n = 3; n<=14; n++) { //12 satelitów
            if (!segments.get(n).isEmpty()) {System.out.print(segments.get(n)+", ");}
        }
        System.out.println(" - numery satelitów wykorzystane do wyznaczenia pozycji (miejsce dla 12 satelitów)");
        System.out.println("\t"+segments.get(15)+" - DOP (dilution of precision) – precyzja wyznaczonej pozycji");
        System.out.println("\t"+segments.get(16)+" - HDOP (horizontal dilution of precision) – horyzontalna precyzja wyznaczonej pozycji");
        System.out.println("\t"+segments.get(17)+" - VDOP (vertical dilution of precision) – wertykalna precyzja wyznaczonej pozycji");
        System.out.println("\t*"+segments.get(18)+" - suma kontrolna");
    }
    private static void GGASequence(String s) {
        String line = s;
        System.out.println("Dostarcza informacji na temat aktualnego położenia:");
        System.out.println(line);
        System.out.println("gdzie:");
        System.out.println("\t"+getStandardSequences(s).substring(2,5)+" - nagłówek");
        List<String> segments = splitString(line);
        String timeUTC = segments.get(1);
        System.out.println("\t"+timeUTC+" - aktualność danych - "+timeUTC.substring(0,2) + ":"+timeUTC.substring(2,4)+":"+timeUTC.substring(4,6));
        String latitude = segments.get(2);
        System.out.println("\t"+latitude+","+segments.get(3)+" - szerokość geograficzna (latitude) - " +latitude.substring(0,2)+" deg "+latitude.substring(2,8) + "' " +segments.get(3));
        String longitude = segments.get(4);
        System.out.println("\t"+longitude+","+segments.get(5)+" - długość geograficzna (longitude) - "+longitude.substring(0,2)+" deg "+longitude.substring(2,9) + "' " +segments.get(5));
        System.out.println("\t"+segments.get(6)+" - jakość pomiaru");
        System.out.println("\t"+segments.get(7)+" - liczba śledzonych satelitów");
        System.out.println("\t"+segments.get(8)+" - horyzontalna dokładność pozycji (HDOP)");
        System.out.println("\t"+segments.get(9)+","+segments.get(10)+" - wysokość w metrach nad poziom morza");
        System.out.println("\t"+segments.get(11)+","+segments.get(12)+" - wysokość geoid (powyżej elipsoidy WGS84)");
        System.out.println("\t(puste pole) – czas od czasu ostatniego uaktualnienia DGPS (uaktualnienia różnicowe wysyłane przez stacje naziemne - zwiększają dokładność wykonywanych pomiarów)");
        System.out.println("\t(puste pole) – numer ID stacji DGPS");
        System.out.println("\t*"+segments.get(15)+" - suma kontrolna");
    }
    private static String getStandardSequences(String s) {
        return s.substring(1, 6);
    }
    public static List<String> splitString(String input) {
        List<String> segments = new ArrayList<>();
        StringBuilder currentSegment = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == ',' || c == '*') {
                segments.add(currentSegment.toString());
                currentSegment = new StringBuilder();
            } else {
                currentSegment.append(c);
            }
        }
        segments.add(currentSegment.toString()); // dodaj ostatni segment
        return segments;
    }
}