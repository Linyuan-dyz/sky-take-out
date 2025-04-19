package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeEditDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 将密码使用md5进行加密，结果和储存的加密密码进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }


    @Override
    public void save(EmployeeDTO employeeDTO) {
        //将dto对象中的值复制到employee中去
        Employee employee = new Employee();
        System.out.println(employeeDTO);
        BeanUtils.copyProperties(employeeDTO, employee);

        //设置默认状态，默认密码，创建时间和更新时间
        employee.setStatus(StatusConstant.ENABLE);
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //设置操作人id
        //通过localThread获取操作人id并设置内容
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }

    @Override
    public PageResult queryPage(EmployeePageQueryDTO employeePageQueryDTO) {
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        Long total = page.getTotal();
        List<Employee> records = page.getResult();
        System.out.println(total);
        System.out.println(records);
        return new PageResult(total, records);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();
        employeeMapper.update(employee);
    }

    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("******");
        return employee;
    }

    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.update(employee);
    }

    @Override
    public void editPassword(EmployeeEditDTO employeeEditDTO) {
        //获取当前操作者的id，如果和被修改用户的id相同才会允许操作,否则抛出异常
        Long currentId = BaseContext.getCurrentId();
        System.out.println(currentId);
        Long empId = employeeEditDTO.getId();
        System.out.println(empId);
        if (!currentId.equals(empId)) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_EDIT);
        }

        //将用户密码和输入的旧密码进行比对，正确时才会允许操作，否则抛出异常
        Employee employee = employeeMapper.getById(empId);
        String pwDB = employee.getPassword();
        String oldPassword = DigestUtils.md5DigestAsHex(employeeEditDTO.getOldPassword().getBytes());
        if (!oldPassword.equals(pwDB)) {
            throw new PasswordErrorException(MessageConstant.OLDPASSWORD_ERROR);
        }

        //更改用户密码为新密码
        String newPassword = DigestUtils.md5DigestAsHex(employeeEditDTO.getNewPassword().getBytes());
        employee.setPassword(newPassword);
        employeeMapper.update(employee);
    }
}
