package Model;

// import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
// import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
/* ObjectMapper om = new ObjectMapper();
Root root = om.readValue(myJsonString, Root.class); */
public class Location {
    public double lat;
    public double lon;

    public Location(double lat, double lon) {
        this.lat=lat;
        this.lon=lon;
    }
}
