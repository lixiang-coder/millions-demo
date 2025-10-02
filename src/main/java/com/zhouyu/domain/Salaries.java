package com.zhouyu.domain;

import org.springframework.beans.factory.InitializingBean;

import javax.annotation.PostConstruct;
import java.util.Date;

/**
 * 作者：周瑜大都督
 */
public class Salaries {

    private Integer empNo;
    private Integer salary;
    private Date fromDate;
    private Date toDate;

    public Integer getEmpNo() {
        return empNo;
    }

    public void setEmpNo(Integer empNo) {
        this.empNo = empNo;
    }

    public Integer getSalary() {
        return salary;
    }

    public void setSalary(Integer salary) {
        this.salary = salary;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }
}
