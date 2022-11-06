package com.OurInternfactory.Services.Impl;

import com.OurInternfactory.Exceptions.ResourceNotFoundException;
import com.OurInternfactory.Models.Category;
import com.OurInternfactory.Models.Internships;
import com.OurInternfactory.Models.User;
import com.OurInternfactory.Payloads.InternshipResponse;
import com.OurInternfactory.Payloads.InternshipsDto;
import com.OurInternfactory.Repositories.CategoryRepo;
import com.OurInternfactory.Repositories.InternshipRepo;
import com.OurInternfactory.Repositories.UserRepo;
import com.OurInternfactory.Services.InternshipServices;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InternshipServiceImpl implements InternshipServices {
    private final InternshipRepo internshipRepo;
    private final ModelMapper modelMapper;
    private final UserRepo userRepo;

    private final CategoryRepo categoryRepo;

    public InternshipServiceImpl(InternshipRepo internshipRepo, ModelMapper modelMapper, UserRepo userRepo, CategoryRepo categoryRepo) {
        this.internshipRepo = internshipRepo;
        this.modelMapper = modelMapper;
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
    }

    @Override
    public InternshipsDto createInternship(InternshipsDto internshipsDto, Integer categoryId) {
        Category category = this.categoryRepo.findById(categoryId).orElseThrow(()-> new ResourceNotFoundException("Category", "CategoryID", categoryId));
        Internships internships = this.modelMapper.map(internshipsDto, Internships.class);
        internships.setImageUrl("default.png");
        internships.setIssuedDate(new Date());
        internships.setCategory(category);
        category.getInternshipsList().add(internships);
        Internships newInternship = this.internshipRepo.save(internships);
        return this.modelMapper.map(newInternship, InternshipsDto.class);
    }
    @Override
    public String applyForInternship(String email, String Title){
        User user = this.userRepo.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User", "Email :"+email, 0));
        Internships internships= this.internshipRepo.findByTitle(Title);
        user.getInterships().add(internships);
        internships.getUser().add(user);
        internshipRepo.save(internships);
        userRepo.save(user);
        return "Internship Applied Successfully";
    }
    @Override
    public InternshipsDto updateInternship(InternshipsDto internshipsDto, Integer internshipId) {
        Internships internships = this.internshipRepo.findById(internshipId).orElseThrow(()-> new ResourceNotFoundException("Internship", "InternshipId", internshipId));
        internships.setTitle(internshipsDto.getTitle());
        internships.setAbout(internshipsDto.getAbout());
        internships.setTenure(internshipsDto.getTenure());
        internships.setLastDate(internshipsDto.getLastDate());
        internships.setStipend(internshipsDto.getStipend());
        internships.setIssuedDate(new Date());
        internships.setImageUrl(internshipsDto.getImageUrl());
        this.internshipRepo.save(internships);
        return this.modelMapper.map(internships, InternshipsDto.class);
    }
    @Override
    public void deleteInternship(Integer internshipId) {
        Internships internships = this.internshipRepo.findById(internshipId).orElseThrow(()-> new ResourceNotFoundException("Internship", "InternshipId", internshipId));
        this.internshipRepo.delete(internships);
    }
    @Override
    public InternshipResponse getAllInternships(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {
//        int pageSize = 5;
//        int pageNumber = 1;
        Sort sort = null;
        if(sortDir.equalsIgnoreCase("asc")){
            sort = Sort.by(sortBy).ascending();
        }
        else{
            sort = Sort.by(sortBy).descending();
        }
        Pageable p = PageRequest.of(pageNumber, pageSize, sort);
        Page<Internships> pageInternships = this.internshipRepo.findAll(p);
        List<Internships> allInternships = pageInternships.getContent();
        List<InternshipsDto> allInternshipsDto = allInternships.stream().map((internships) -> this.modelMapper.map(internships, InternshipsDto.class)).collect(Collectors.toList());

        return new InternshipResponse(allInternshipsDto, pageInternships.getNumber(), pageInternships.getSize(), pageInternships.getTotalPages(), pageInternships.getTotalElements(), pageInternships.isLast());
    }

    @Override
    public InternshipsDto getSingleInternship(Integer internshipId) {
        Internships internships = this.internshipRepo.findById(internshipId).orElseThrow(()->new ResourceNotFoundException("Internship", "internshipId", internshipId));
        return this.modelMapper.map(internships, InternshipsDto.class);
    }

    @Override
    public InternshipResponse getInternshipsByCategory(Integer categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {
        Category category = this.categoryRepo.findById(categoryId).orElseThrow(()-> new ResourceNotFoundException("Category", "Category Id", categoryId));
        Sort sort = null;
        if(sortDir.equalsIgnoreCase("asc")){
            sort = Sort.by(sortBy).ascending();
        }
        else{
            sort = Sort.by(sortBy).descending();
        }
        Pageable p = PageRequest.of(pageNumber, pageSize, sort);
        Page<Internships> pageInternships = this.internshipRepo.findByCategory(category, p);//.findByCategory(category);
        List<Internships> allInternships = pageInternships.getContent();
        List<InternshipsDto> allInternshipsDto = allInternships.stream().map((internships) -> this.modelMapper.map(internships, InternshipsDto.class)).collect(Collectors.toList());
        return new InternshipResponse(allInternshipsDto, pageInternships.getNumber(), pageInternships.getSize(), pageInternships.getTotalPages(), pageInternships.getTotalElements(), pageInternships.isLast());
    }

    @Override
    public InternshipResponse getInternshipsByUser(Integer userId, Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {
        User user = this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User", "userID", userId));
        Sort sort = null;
        if(sortDir.equalsIgnoreCase("asc")){
            sort = Sort.by(sortBy).ascending();
        }
        else{
            sort = Sort.by(sortBy).descending();
        }
        Pageable p = PageRequest.of(pageNumber, pageSize, sort);
        Page<Internships> pageInternships = this.internshipRepo.findByUser(user, p);
        List<Internships> allInternships = pageInternships.getContent();
        List<InternshipsDto> allInternshipsDto = allInternships.stream().map((internships) -> this.modelMapper.map(internships, InternshipsDto.class)).collect(Collectors.toList());
        return new InternshipResponse(allInternshipsDto, pageInternships.getNumber(), pageInternships.getSize(), pageInternships.getTotalPages(), pageInternships.getTotalElements(), pageInternships.isLast());
    }
    @Override
    public InternshipResponse searchInternships(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDir){
//        User user = this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User", "userID", userId));
        Sort sort = null;
        if(sortDir.equalsIgnoreCase("asc")){
            sort = Sort.by(sortBy).ascending();
        }
        else{
            sort = Sort.by(sortBy).descending();
        }
        Pageable p = PageRequest.of(pageNumber, pageSize, sort);
        Page<Internships> pageInternships = this.internshipRepo.findByTitleContainingIgnoreCase(keyword, p);
        List<Internships> allInternships = pageInternships.getContent();
        List<InternshipsDto> allInternshipsDto = allInternships.stream().map((internships) -> this.modelMapper.map(internships, InternshipsDto.class)).collect(Collectors.toList());
        return new InternshipResponse(allInternshipsDto, pageInternships.getNumber(), pageInternships.getSize(), pageInternships.getTotalPages(), pageInternships.getTotalElements(), pageInternships.isLast());
    }
}
