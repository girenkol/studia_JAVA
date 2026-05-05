import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool; 
import java.util.concurrent.RecursiveTask; 

class MaxFindTask extends RecursiveTask<int[]> // framework zwraca tablicę dwóch intów
{

    private static final int THRESHOLD = 10_000; //Ile elementów ma byc w ostatecznym podzieleniu
    
    private final int[] array;
    private final int start;
    private final int end; 

    public MaxFindTask(int[] array, int start, int end) //Przypisanie danych do podzadania
    { 
        this.array = array;
        this.start = start; 
        this.end = end; // Zapisanie indeksów dla podzadania
    }

    @Override 
    protected int[] compute() // Główna metoda
    {
        int length = end - start; // Obliczenie długości aktualnie rozpatrywanego fragmentu tablicy

        if (length <= THRESHOLD) // Sprawdzenie czy fragment tablicy jest już wystarczająco mały
        { 
            return computeSequentially(); 
        } 

        else //Dzielenie dalej
        { 
            int centr = start + length / 2; 

            MaxFindTask leftTwix = new MaxFindTask(array, start, centr); 
            MaxFindTask rightTwix = new MaxFindTask(array, centr, end); //Podzial na 2 mniejsze

            leftTwix.fork(); // Lewa część na nowy wątek
            int[] rightResult = rightTwix.compute(); // Prawa część sie wykonuje
            int[] leftResult = leftTwix.join(); // Oczekiwanie az lewa sie wykona i wykonanie lewej

            int[] combined = {leftResult[0], leftResult[1], rightResult[0], rightResult[1]}; // Zlaczenie 2 najlepszych wyników z lewej i prawej
            Arrays.sort(combined); 
            
            return new int[]{combined[3], combined[2]}; //Wybór największych z posortowanej tablicy
        }
    }

    private int[] computeSequentially() { //Znajduje 2 najwieksze w podzielonej grupie
        int max1 = Integer.MIN_VALUE; // Największa
        int max2 = Integer.MIN_VALUE; // Druga największa, przypisano wartość początkową - najmniejszą
         
        for (int i = start; i < end; i++) // Przejście pętlą do znalezienia najwiekszego
            {
            if (array[i] > max1)//Dopasowanie co jest większe od czego
                { 
                    max2 = max1; 
                    max1 = array[i]; 
                }
                else if (array[i] > max2)
                { 
                     max2 = array[i]; 
                } 
        } 
        return new int[]{max1, max2}; //Najwięksi z 10k
    } 

    public static void main(String[] args) 
    { 
        try 
        {
            Thread.sleep(5000);
        } 
        catch (InterruptedException e) {}//delay, bo profiler nie wyrabia

        int arraySize = 950_000_000; // Ilosc elementów do random
        int[] data = new int[arraySize]; 
        Random random = new Random(); // RNG

        for (int i = 0; i < arraySize; i++) // RNG wypełnienie losowe do 750.000.000
        { 
            data[i] = random.nextInt(999_000_000);
        } 
        
        data[arraySize / 2] = Integer.MAX_VALUE; //największe co może być, żeby pokazać, że działa

        ForkJoinPool pool = new ForkJoinPool(); //Powstanie ForkJoin
        //MaxFindTask mainTask = new MaxFindTask(data, 0, data.length); // Rozpoczecie dzialania z 50.000.000 elementów
        
        //int[] top = pool.invoke(mainTask); // Zlecenie zadania i odbiór wyników
        
        int[] top = null; // Zmienna na ostateczny wynik
        
        for(int i = 0; i < 12; i++) //12 razy ta sama robota, bo procesor za szybko robi i nie widac na profilerze
        {
            MaxFindTask mainTask = new MaxFindTask(data, 0, data.length); 
            top = pool.invoke(mainTask);
        }

        System.out.println("Ustawiony wygrany: " + top[0]); 
        System.out.println("Prawdzwy wygrany: " + top[1]);
        
        try 
        {
            Thread.sleep(5000);
        } 
        catch (InterruptedException e) {}//delay, bo profiler nie wyrabia

        pool.shutdown(); 

    }
} 
