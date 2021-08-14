public class Secretariat {
    public static String getConfirmationCode(String course, String id, int vacancies) throws InterruptedException {
        return course+'-'+id+'-'+ vacancies;
    }
}
