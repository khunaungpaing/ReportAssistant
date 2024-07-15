package com.khun.reportassistant.models;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class NecessaryData {
    List<String> focMember;
    List<String> removeMember;
}
