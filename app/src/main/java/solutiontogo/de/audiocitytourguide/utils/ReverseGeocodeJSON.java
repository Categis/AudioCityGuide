package solutiontogo.de.audiocitytourguide.utils;

/**
 * Created by maheshkandhari on 4/12/2017.
 */

import lombok.Data;
import java.util.List;

@Data
public class ReverseGeocodeJSON {

    public List<Result> results = null;

    public class Result {
        public String formattedAddress;
        public String placeId;
    }

}