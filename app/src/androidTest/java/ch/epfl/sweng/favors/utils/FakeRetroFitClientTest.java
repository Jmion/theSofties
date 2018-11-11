package ch.epfl.sweng.favors.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import ch.epfl.sweng.favors.utils.Retrofit.RetrofitDispatcher;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;

public class FakeRetroFitClientTest {

    @Before
    public void before(){
        ExecutionMode.getInstance().setTest(true);
    }

    @Test
    public void getApiWorks() throws IOException{
        RetrofitDispatcher.getInstance().getApi().sendEmail("from", "to", "subject", "test").cancel();
        Call<ResponseBody> cloned = RetrofitDispatcher.getInstance().getApi().sendEmail("from", "to", "subject", "test").clone();
        assertEquals(null, cloned);
        Response<ResponseBody> executed = RetrofitDispatcher.getInstance().getApi().sendEmail("from", "to", "subject", "test").execute();
        assertEquals(null, executed);
        boolean isExecuted = RetrofitDispatcher.getInstance().getApi().sendEmail("from", "to", "subject", "test").isExecuted();
        assertEquals(true, isExecuted);
        Request request = RetrofitDispatcher.getInstance().getApi().sendEmail("from", "to", "subject", "test").request();
        assertEquals(null, request);
    }

    @After
    public void after(){
        ExecutionMode.getInstance().setTest(false);
    }
}
