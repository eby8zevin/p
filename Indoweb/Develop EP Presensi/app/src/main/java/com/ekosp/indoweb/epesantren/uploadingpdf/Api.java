package com.ekosp.indoweb.epesantren.uploadingpdf;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface Api {
    @FormUrlEncoded
    @POST("restapi/upload_document.php")
    Call<ResponsePOJO> uploadDocument(
            @Field("PDF") String encodedPDF
    );
}
