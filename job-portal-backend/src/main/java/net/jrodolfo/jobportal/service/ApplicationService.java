package net.jrodolfo.jobportal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.jrodolfo.jobportal.model.Application;
import net.jrodolfo.jobportal.model.Job;
import net.jrodolfo.jobportal.model.User;
import net.jrodolfo.jobportal.repository.ApplicationRepository;
import net.jrodolfo.jobportal.repository.JobRepository;
import net.jrodolfo.jobportal.repository.UserRepository;

@Service
public class ApplicationService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    @Autowired
    public ApplicationService(UserRepository userRepository,
                              JobRepository jobRepository,
                              ApplicationRepository applicationRepository) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    public Application applyForJob(String username, Long jobId) {
        /* Fetch User object using userId or throw exception */
        User user = userRepository.findByName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        /* Fetch Job object using jobId or throw exception */
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        /* Attach User and Job details to the application object */
        Application application = new Application(user, job);
        /* Save it in DB using save() method of JPA Repository */
        return applicationRepository.save(application);
    }
}
