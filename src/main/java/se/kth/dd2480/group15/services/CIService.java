package se.kth.dd2480.group15.services;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Service;

import se.kth.dd2480.group15.api.dto.request.PushRequestDTO;
import se.kth.dd2480.group15.domain.Build;

@Service
public class CIService {

    /** Queue holding incoming build jobs waiting to be processed. */
    private BlockingQueue<Build> queue = new LinkedBlockingQueue<>();

    /** Background worker thread responsible for handling queued builds. */
    private Thread thread;


    public CIService() { }


    public void queueJob(PushRequestDTO job) { }

    public void handleJob(Build job) { }


    public void startThread() { }

    public void stopThread() { }

    private void runThread() { }
}
