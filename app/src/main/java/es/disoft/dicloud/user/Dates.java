package es.disoft.dicloud.user;

public class Dates {
    public static synchronized void setTimeAlert(int time) {
        System.out.println("TIME IS = " + time);
        /*try {
            System.out.println("ENTRO EN EL TRY");
            String url = mContext.getString(R.string.URL_GET_DATES);
            String jsonResponse = jsonRequest(new URL(url));
            updateDates(jsonResponse);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }*/
    }
}
