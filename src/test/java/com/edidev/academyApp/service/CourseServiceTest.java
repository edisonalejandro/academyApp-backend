package com.edidev.academyApp.service;

import com.edidev.academyApp.dto.CourseDTO;
import com.edidev.academyApp.enums.DanceLevel;
import com.edidev.academyApp.enums.DanceType;
import com.edidev.academyApp.exception.DuplicateResourceException;
import com.edidev.academyApp.exception.ResourceNotFoundException;
import com.edidev.academyApp.model.Course;
import com.edidev.academyApp.model.User;
import com.edidev.academyApp.repository.CourseRepository;
import com.edidev.academyApp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CourseService courseService;

    private Course testCourse;
    private User testTeacher;
    private CourseDTO testCourseDTO;

    @BeforeEach
    void setUp() {
        testTeacher = new User();
        testTeacher.setId(1L);
        testTeacher.setEmail("teacher@test.com");
        testTeacher.setFirstName("John");
        testTeacher.setLastName("Doe");

        testCourse = Course.builder()
                .id(1L)
                .title("Salsa Básica")
                .code("SALSA-001")
                .description("Curso de salsa para principiantes")
                .danceType(DanceType.SALSA)
                .level(DanceLevel.BEGINNER)
                .pricePerHour(new BigDecimal("50.00"))
                .durationHours(new BigDecimal("10"))
                .maxCapacity(20)
                .teacher(testTeacher)
                .isActive(true)
                .build();

        testCourseDTO = CourseDTO.builder()
                .title("Salsa Básica")
                .code("SALSA-001")
                .description("Curso de salsa para principiantes")
                .danceType(DanceType.SALSA)
                .level(DanceLevel.BEGINNER)
                .pricePerHour(new BigDecimal("50.00"))
                .durationHours(new BigDecimal("10"))
                .maxCapacity(20)
                .teacherId(1L)
                .build();
    }

    @Test
    void testGetAllActiveCourses() {
        // Given
        List<Course> courses = Arrays.asList(testCourse);
        when(courseRepository.findByIsActiveTrueOrderByTitleAsc()).thenReturn(courses);

        // When
        List<CourseDTO> result = courseService.getAllActiveCourses();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Salsa Básica", result.get(0).getTitle());
        verify(courseRepository, times(1)).findByIsActiveTrueOrderByTitleAsc();
    }

    @Test
    void testGetCourseById() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));

        // When
        CourseDTO result = courseService.getCourseById(1L);

        // Then
        assertNotNull(result);
        assertEquals("Salsa Básica", result.getTitle());
        assertEquals("SALSA-001", result.getCode());
        verify(courseRepository, times(1)).findById(1L);
    }

    @Test
    void testGetCourseById_NotFound() {
        // Given
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            courseService.getCourseById(999L);
        });
        verify(courseRepository, times(1)).findById(999L);
    }

    @Test
    void testCreateCourse() {
        // Given
        when(courseRepository.existsByCodeAndIsActiveTrue(anyString())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // When
        CourseDTO result = courseService.createCourse(testCourseDTO);

        // Then
        assertNotNull(result);
        assertEquals("Salsa Básica", result.getTitle());
        assertEquals("SALSA-001", result.getCode());
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void testCreateCourse_DuplicateCode() {
        // Given
        when(courseRepository.existsByCodeAndIsActiveTrue("SALSA-001")).thenReturn(true);

        // When & Then
        assertThrows(DuplicateResourceException.class, () -> {
            courseService.createCourse(testCourseDTO);
        });
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void testUpdateCourse() {
        // Given
        CourseDTO updateDTO = CourseDTO.builder()
                .title("Salsa Avanzada")
                .description("Curso de salsa avanzado")
                .pricePerHour(new BigDecimal("75.00"))
                .teacherId(1L)  // Añadir teacherId
                .build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testTeacher)); // Mock del teacher
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // When
        CourseDTO result = courseService.updateCourse(1L, updateDTO);

        // Then
        assertNotNull(result);
        verify(courseRepository, times(1)).findById(1L);
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void testDeleteCourse() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // When
        courseService.deleteCourse(1L);

        // Then
        verify(courseRepository, times(1)).findById(1L);
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void testToggleCourseStatus() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // When
        CourseDTO result = courseService.toggleCourseStatus(1L);

        // Then
        assertNotNull(result);
        verify(courseRepository, times(1)).findById(1L);
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void testGetCoursesByDanceType() {
        // Given
        List<Course> courses = Arrays.asList(testCourse);
        when(courseRepository.findByDanceTypeAndIsActiveTrueOrderByLevelAsc(DanceType.SALSA))
                .thenReturn(courses);

        // When
        List<CourseDTO> result = courseService.getCoursesByDanceType(DanceType.SALSA);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(DanceType.SALSA, result.get(0).getDanceType());
    }

    @Test
    void testGetCoursesByLevel() {
        // Given
        List<Course> courses = Arrays.asList(testCourse);
        when(courseRepository.findByLevelAndIsActiveTrueOrderByTitleAsc(DanceLevel.BEGINNER))
                .thenReturn(courses);

        // When
        List<CourseDTO> result = courseService.getCoursesByLevel(DanceLevel.BEGINNER);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(DanceLevel.BEGINNER, result.get(0).getLevel());
    }
}
