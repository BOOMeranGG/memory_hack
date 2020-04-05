package com.donatella.demo.model.repositories

import com.donatella.demo.model.entities.Veteran
import org.springframework.data.jpa.repository.JpaRepository

interface VeteranRepository : JpaRepository<Veteran, String> {
}