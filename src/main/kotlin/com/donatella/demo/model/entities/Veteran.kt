package com.donatella.demo.model.entities

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "veterans")
class Veteran {

    @Id
    @Column(name = "veteran_id")
    var veteran_id: String = UUID.randomUUID().toString()

    @ManyToOne
    var user: User? = null

    @Column(name = "veteran_name")
    var veteranName: String? = null

    @Column(name = "veteran_photo_index")
    var veteranPhotoIndex: Int = 1
}