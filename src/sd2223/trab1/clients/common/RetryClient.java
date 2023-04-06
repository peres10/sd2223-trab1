package sd2223.trab1.clients.common;

import jakarta.ws.rs.ProcessingException;
import sd2223.trab1.api.service.java.Result;

import java.util.function.Supplier;
import java.util.logging.Logger;

public class RetryClient {
    private static Logger Log = Logger.getLogger(RetryClient.class.getName());

    protected static final int READ_TIMEOUT = 5000;
    protected static final int CONNECT_TIMEOUT = 5000;

    protected static final int MAX_RETRIES = 10;
    protected static final int RETRY_SLEEP = 3000;

    protected <T> Result<T> reTry(Supplier<Result<T>> func){
        return this.reTry(func, MAX_RETRIES);
    }

    protected <T> Result<T> reTry(Supplier<Result<T>> func, int numRetries){
        for(int i=0; i<MAX_RETRIES; i++)
            try{
                return func.get();
            } catch (ProcessingException x){
                Log.fine("Timeout: " + x.getMessage());
                sleep_ms(RETRY_SLEEP);
            } catch (Exception x){
                return Result.error(Result.ErrorCode.INTERNAL_ERROR);
            }
        return Result.error(Result.ErrorCode.INTERNAL_ERROR);
    }

    private void sleep_ms(int ms){
        try{
            Thread.sleep(ms);
        } catch(InterruptedException e){
            e.printStackTrace();
        }
    }
}
