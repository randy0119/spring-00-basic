package com.example.spring_01_boot.point.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.spring_01_boot.point.repository.entity.Point;

public interface PointRepository extends JpaRepository<Point, String> {

}
