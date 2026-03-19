package com.example.spring_01_boot.point.service;

import com.example.spring_01_boot.point.repository.PointRepository;
import com.example.spring_01_boot.point.repository.entity.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PointServiceImplTest {

    private static final String TEST_USER_ID = "point-test-user";

    @Autowired
    private PointService pointService;

    @Autowired
    private PointRepository pointRepository;

    @AfterEach
    void tearDown() {
        pointRepository.deleteById(TEST_USER_ID);
    }

    @Test
    void getPoint_whenUserRowDoesNotExist_returnsZeroAndCreatesRow() {
        // given
        pointRepository.deleteById(TEST_USER_ID);

        // when
        int balance = pointService.getPoint(TEST_USER_ID);

        // then
        assertThat(balance).isEqualTo(0);

        Optional<Point> createdPoint = pointRepository.findById(TEST_USER_ID);
        assertThat(createdPoint).isPresent();
        assertThat(createdPoint.get().getBalance()).isEqualTo(0L);
    }

    @Test
    void getPoint_whenCalledTwice_doesNotCreateDuplicateRow() {
        // given
        pointRepository.deleteById(TEST_USER_ID);

        // when
        int firstBalance = pointService.getPoint(TEST_USER_ID);
        long countAfterFirstCall = pointRepository.count();
        int secondBalance = pointService.getPoint(TEST_USER_ID);
        long countAfterSecondCall = pointRepository.count();

        // then
        assertThat(firstBalance).isEqualTo(0);
        assertThat(secondBalance).isEqualTo(0);
        assertThat(countAfterSecondCall).isEqualTo(countAfterFirstCall);
        assertThat(pointRepository.findById(TEST_USER_ID)).isPresent();
    }
}
