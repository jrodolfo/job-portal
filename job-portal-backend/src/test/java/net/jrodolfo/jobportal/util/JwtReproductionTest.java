package net.jrodolfo.jobportal.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JwtReproductionTest {

    private JwtUtil createJwtUtil() {
        JwtUtil jwtUtil = new JwtUtil();
        // Manually inject the secret key for tests since we are not using @SpringBootTest here
        try {
            java.lang.reflect.Field field = JwtUtil.class.getDeclaredField("secretKey");
            field.setAccessible(true);
            field.set(jwtUtil, "MY_SECRET_KEY_123456789012345678901234567890");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return jwtUtil;
    }

    @Test
    public void testExtractionWithGoogleToken() {
        JwtUtil jwtUtil = createJwtUtil();
        String googleToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6Ijg2MzBhNzFiZDZlYzFjNjEyNTdhMjdmZjJlZmQ5MTg3MmVjYWIxZjYiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1OTcxNjgxNjMzMTgtZ2FmZWplZWg3cnVscm03dHNuanRwdmQ3ampkM3ZzYWMuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1OTcxNjgxNjMzMTgtZ2FmZWplZWg3cnVscm03dHNuanRwdmQ3ampkM3ZzYWMuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTM0NTc2NjA1NjY0NTI3MjM5MTgiLCJlbWFpbCI6Impyb2RvbGZvQGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJhdF9oYXNoIjoiczNLdXg5N0JXUVFaRHN5LXdyQWFQdyIsIm5vbmNlIjoiV0lhLUQ2SENpVmhGMWRKM290bG5RcFNJcklLNTRzS3ZzS2RRMVd1Z29YdyIsIm5hbWUiOiJSb2QgT2xpdmVpcmEiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EvQUNnOG9jSzZUeUhZcTdkVGN1MWpFenZIOWUwaUxSeTk2SW5kNGd6MXdiOXdOQXlRZUJJcFFaRE11dz1zOTYtYyIsImdpdmVuX25hbWUiOiJSb2QiLCJmYW1pbHlfbmFtZSI6Ik9saXZlaXJhIiwiaWF0IjoxNzY5OTY1MTI1LCJleHAiOjE3Njk5Njg3MjV9.G8_Gh7yj2odK54laEf6P6M3AjvCCTz8xZYFsI-AnIF7bbhQBqd80HoR7223YB6mo6_URA4ARTNXhNNWMstCMFmcm4_smq2wNsZaldjlBwDEKqrB08arcFuqjaL2KCH0IacCuv1IZOtoIq1AfcLRVVf__vW2htzMX94lUt4diC9pBr5-kS0vhTsu0TB-jp60nwuo4VMUTqm4KvrxjLjMB7NDwiP99RRyNMz8cljK05AoEaxDCqtM4cYdSfQvEfMfScv1Y-EDw1kTivN--hcNaJX9K9DMaY_hXfxGGBzb1E7vHi58lU5Bq5gOQb2M9Aw-XpkAPf9s3hm0JnSBDRBWM-A";
        
        String email = jwtUtil.extractEmail(googleToken);
        assertEquals("jrodolfo@gmail.com", email);
    }

    @Test
    public void testValidationWithGoogleToken() {
        JwtUtil jwtUtil = createJwtUtil();
        String googleToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6Ijg2MzBhNzFiZDZlYzFjNjEyNTdhMjdmZjJlZmQ5MTg3MmVjYWIxZjYiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI1OTcxNjgxNjMzMTgtZ2FmZWplZWg3cnVscm03dHNuanRwdmQ3ampkM3ZzYWMuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI1OTcxNjgxNjMzMTgtZ2FmZWplZWg3cnVscm03dHNuanRwdmQ3ampkM3ZzYWMuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTM0NTc2NjA1NjY0NTI3MjM5MTgiLCJlbWFpbCI6Impyb2RvbGZvQGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJhdF9oYXNoIjoiczNLdXg5N0JXUVFaRHN5LXdyQWFQdyIsIm5vbmNlIjoiV0lhLUQ2SENpVmhGMWRKM290bG5RcFNJcklLNTRzS3ZzS2RRMVd1Z29YdyIsIm5hbWUiOiJSb2QgT2xpdmVpcmEiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EvQUNnOG9jSzZUeUhZcTdkVGN1MWpFenZIOWUwaUxSeTk2SW5kNGd6MXdiOXdOQXlRZUJJcFFaRE11dz1zOTYtYyIsImdpdmVuX25hbWUiOiJSb2QiLCJmYW1pbHlfbmFtZSI6Ik9saXZlaXJhIiwiaWF0IjoxNzY5OTY1MTI1LCJleHAiOjIxNDc0ODM2NDd9.G8_Gh7yj2odK54laEf6P6M3AjvCCTz8xZYFsI-AnIF7bbhQBqd80HoR7223YB6mo6_URA4ARTNXhNNWMstCMFmcm4_smq2wNsZaldjlBwDEKqrB08arcFuqjaL2KCH0IacCuv1IZOtoIq1AfcLRVVf__vW2htzMX94lUt4diC9pBr5-kS0vhTsu0TB-jp60nwuo4VMUTqm4KvrxjLjMB7NDwiP99RRyNMz8cljK05AoEaxDCqtM4cYdSfQvEfMfScv1Y-EDw1kTivN--hcNaJX9K9DMaY_hXfxGGBzb1E7vHi58lU5Bq5gOQb2M9Aw-XpkAPf9s3hm0JnSBDRBWM-A";
        
        boolean isValid = jwtUtil.validateToken(googleToken, "jrodolfo@gmail.com");
        assertTrue(isValid);
    }
}
