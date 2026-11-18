package group2026.cozylife;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CozyLifeLoginRequest(
        String mail,
        String passwd,
        String lang,
        String platform,
        String imei,
        String lat,
        String lng,
        String country_number_code,
        String package_name,
        String user_term_version,
        String user_privacy_version,
        String package_version
) {

    private static final String LANG = "en";
    private static final String PLATFORM = "ios";
    private static final String IMEI = "354627900145865";
    private static final String LAT = "53.67";
    private static final String LNG = "23.86";
    private static final String COUNTRY_CODE = "112";
    private static final String PACKAGE_NAME = "am.doit.cozylife";
    private static final String TERM_VERSION = "1.0.1";
    private static final String PRIVACY_VERSION = "1.0.0";
    private static final String PACKAGE_VERSION = "1.20.4";

    public static CozyLifeLoginRequest of(String mail, String passwd) {
        return new CozyLifeLoginRequest(
                mail,
                passwd,
                LANG,
                PLATFORM,
                IMEI,
                LAT,
                LNG,
                COUNTRY_CODE,
                PACKAGE_NAME,
                TERM_VERSION,
                PRIVACY_VERSION,
                PACKAGE_VERSION);
    }
}