package com.campus.repository;

import com.campus.model.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobRepository extends JpaRepository<Job, Long> {
    
    @Query(value = "SELECT * FROM jobs j WHERE " +
           "(:keyword IS NULL OR j.title LIKE CONCAT('%',:keyword,'%') OR j.description LIKE CONCAT('%',:keyword,'%')) AND " +
           "(:location IS NULL OR j.location LIKE CONCAT('%',:location,'%')) AND " +
           "(:education IS NULL OR j.education_requirement = :education) AND " +
           "(:positionType IS NULL OR j.position_type = :positionType) AND " +
           "(:salary IS NULL OR j.salary LIKE CONCAT('%',:salary,'%')) AND " +
           "j.status = 'PUBLISHED'", 
           nativeQuery = true,
           countQuery = "SELECT count(*) FROM jobs j WHERE " +
           "(:keyword IS NULL OR j.title LIKE CONCAT('%',:keyword,'%') OR j.description LIKE CONCAT('%',:keyword,'%')) AND " +
           "(:location IS NULL OR j.location LIKE CONCAT('%',:location,'%')) AND " +
           "(:education IS NULL OR j.education_requirement = :education) AND " +
           "(:positionType IS NULL OR j.position_type = :positionType) AND " +
           "(:salary IS NULL OR j.salary LIKE CONCAT('%',:salary,'%')) AND " +
           "j.status = 'PUBLISHED'")
    Page<Job> searchJobs(@Param("keyword") String keyword, 
                        @Param("location") String location, 
                        @Param("education") String education, 
                        @Param("positionType") String positionType, 
                        @Param("salary") String salary, 
                        Pageable pageable);

    @Modifying
    @Query("UPDATE Job j SET j.viewCount = j.viewCount + 1 WHERE j.id = ?1")
    void incrementViewCount(Long jobId);
    
    @Modifying
    @Query("UPDATE Job j SET j.applyCount = j.applyCount + 1 WHERE j.id = ?1")
    void incrementApplyCount(Long jobId);
    
    Page<Job> findByCompanyId(Long companyId, Pageable pageable);
    
    Page<Job> findByStatus(Job.JobStatus status, Pageable pageable);

    Page<Job> findByTitleContainingOrDescriptionContaining(String title, String description, Pageable pageable);
} 