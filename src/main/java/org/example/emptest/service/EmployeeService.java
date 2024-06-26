package org.example.emptest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.emptest.dto.EmployeeCreateDto;
import org.example.emptest.dto.EmployeeSearchCond;
import org.example.emptest.dto.EmployeeUpdateDto;
import org.example.emptest.entity.Employee;
import org.example.emptest.entity.Equipment;
import org.example.emptest.repository.DepartmentRepository;
import org.example.emptest.repository.EmployeeRepository;
import org.example.emptest.repository.EquipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EquipmentRepository equipmentRepository;

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(int empId) {
        return employeeRepository.findById(empId).get();
    }

    public List<Employee> getByDeptAndEmpTypeAndSalary(EmployeeSearchCond searchCond) {
        return employeeRepository.dynamicSearch(searchCond);
    }

    @Transactional
    public void deleteEmployeeById(int empId) {
        employeeRepository.deleteById(empId);
    }

    @Transactional
    public Employee addEmployee(EmployeeCreateDto employeeDto) {
        // 사원에게 배정되지 않은 장비가 먼저 존재해야 할당할 수 있음, 부서가 이미 등록되어 있어야 부서입력 가능
        // 장비가 없으면 장비를 먼저 등록해야 함
        // 두 연관관계의 주인이 모두 사원이 되어야 함.
        // 입력받은 장비가 이미 존재하고 , 사원에 할당되어 있지 않은 경우 진행 가능

        Employee employee = Employee.createEmployee(
                employeeDto,
                departmentRepository.findById(employeeDto.getDeptId()).get(),
                equipmentRepository.findBySeqno(employeeDto.getSeqno()));

        Employee savedEmp = employeeRepository.save(employee);
        log.info("employee saved: equipment id {} " , savedEmp.getEquipment().getSeqno());
        Equipment savedEq = equipmentRepository.save(savedEmp.getEquipment());
        log.info("equipment saved : employee id {} " , savedEmp.getEquipment().getEmployee().getEmpName());
        return savedEmp;
    }

    @Transactional
    public Employee updateEmployee(EmployeeUpdateDto employeeDto) {
        Employee employee = employeeRepository.findById(employeeDto.getEmpId()).get();
        employee.setSalary(employeeDto.getSalary());
        //employee.setDepartment(employeeDto.getDepartment());
        Employee updateEmployee = employeeRepository.save(employee);
        return updateEmployee;
    }

    @Transactional
    public void changeEquipment(int empId, Equipment equipment) {
        Employee employee = employeeRepository.findById(empId).get();
        Equipment befEquipment = employee.getEquipment();
        employee.changeEquipment(equipment);
        Employee savedEmp = employeeRepository.save(employee);
        Equipment afterEq = equipmentRepository.save(equipment);
        Equipment beforeEq = equipmentRepository.save(befEquipment);
        log.info("employee saved: equipment id {} " , savedEmp.getEquipment().getSeqno());
        log.info("equipment saved : employee id {} " , equipment.getEmployee().getEmpName());
        log.info("equipment before : employee id {} " , befEquipment.getEmployee());
    }

    @Transactional
    public void retireProcess(int empId) {
        Employee employee = employeeRepository.findById(empId).get();
        Equipment equipment = employee.getEquipment();
        employee.retireProcess();

        employeeRepository.save(employee);
        equipmentRepository.save(equipment);
    }
}
