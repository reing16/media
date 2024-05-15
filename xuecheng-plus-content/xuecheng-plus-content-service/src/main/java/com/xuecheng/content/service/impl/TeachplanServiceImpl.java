package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @description 课程计划service接口实现类
 * @author Mr.M
 * @date 2022/9/9 11:14
 * @version 1.0
 */
@Service
public class TeachplanServiceImpl implements TeachplanService {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;
    @Autowired
    CourseTeacherMapper courseTeacherMapper;
    @Override
    public List<TeachplanDto> findTeachplanTree(long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }


    @Transactional
    @Override
    public void saveTeachplan(SaveTeachplanDto teachplanDto) {

        //课程计划id
        Long id = teachplanDto.getId();
        //修改课程计划
        if(id!=null){
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(teachplanDto,teachplan);
            teachplanMapper.updateById(teachplan);
        }else{
            //取出同父同级别的课程计划数量
            int count = getTeachplanCount(teachplanDto.getCourseId(), teachplanDto.getParentid());
            Teachplan teachplanNew = new Teachplan();
            //设置排序号
            teachplanNew.setOrderby(count+1);
            BeanUtils.copyProperties(teachplanDto,teachplanNew);

            teachplanMapper.insert(teachplanNew);

        }

    }

    @Override
    public void delTeachplan(Long planId) {
        Teachplan teachplan = teachplanMapper.selectById(planId);
        if (teachplan == null ){
            XueChengPlusException.cast("课程不存在");
        }
        if (teachplan.getGrade().equals(2)){
            TeachplanMedia teachplanMediaId = teachplanMediaMapper.selectByteachplanId(planId);
            if (teachplanMediaId != null ){
                teachplanMediaMapper.deleteById(teachplanMediaId.getId());
            }
            teachplan.setStatus(0);
            teachplan.setParentid(-1L);

        }else if (teachplan.getGrade().equals(1)){
//            TeachplanDto teachplanDto = teachplanMapper.selectTreeNodesById(planId);
//            if (teachplanDto.getTeachPlanTreeNodes().isEmpty()){
//                teachplan.setStatus(0);
//                teachplan.setParentid(-1L);
//            }else {
//                XueChengPlusException.cast("课程计划信息还有子级信息，无法操作");
//            }
            teachplan.setStatus(0);
            teachplan.setParentid(-1L);
        }
        teachplanMapper.updateById(teachplan);
    }

    @Override
    public void moveUp(Long planId) {
        Teachplan teachplan = teachplanMapper.selectById(planId);
        LambdaQueryWrapper<Teachplan> qw = new LambdaQueryWrapper<>();
        qw.eq(Teachplan::getCourseId, teachplan.getCourseId())
                .eq(Teachplan::getStatus,1)
                .eq(Teachplan::getParentid,teachplan.getParentid())
                .lt(Teachplan::getOrderby,teachplan.getOrderby())
                .orderByDesc(Teachplan::getOrderby)
                .last("limit 1");
        Teachplan last = teachplanMapper.selectOne(qw);
        if (last == null){
            XueChengPlusException.cast("不可移动");
        }else {
            int order = teachplan.getOrderby();
            teachplan.setOrderby(last.getOrderby());
            last.setOrderby(order);
            teachplanMapper.updateById(teachplan);
            teachplanMapper.updateById(last);
        }
    }

    @Override
    public void movdown(Long planId) {
        Teachplan teachplan = teachplanMapper.selectById(planId);
        LambdaQueryWrapper<Teachplan> qw = new LambdaQueryWrapper<>();
        qw.eq(Teachplan::getCourseId, teachplan.getCourseId())
                .eq(Teachplan::getStatus,1)
                .eq(Teachplan::getParentid,teachplan.getParentid())
                .lt(Teachplan::getOrderby,teachplan.getOrderby())
                .orderByDesc(Teachplan::getOrderby)
                .last("limit 1");
        Teachplan first = teachplanMapper.selectOne(qw);
        if (first == null){
            XueChengPlusException.cast("不可移动");
        }else {
            int order = teachplan.getOrderby();
            teachplan.setOrderby(first.getOrderby());
            first.setOrderby(order);
            teachplanMapper.updateById(teachplan);
            teachplanMapper.updateById(first);
        }
    }

    @Override
    public CourseTeacherDto findcourseTeacher(Long courseId) {
        CourseTeacher courseTeacher = courseTeacherMapper.selectCourseById(courseId);

        if (courseTeacher == null){
            XueChengPlusException.cast("查不到该课程的老师");
        }
        CourseTeacherDto courseTeacherDto = new CourseTeacherDto();
        BeanUtils.copyProperties(courseTeacher,courseTeacherDto);

        return courseTeacherDto;
    }

    @Override
    public boolean addCourseTeacher(Long companyId, CourseTeacherDto courseTeacherDto) {
        CourseTeacherDto courseTeacherDtoNew = new CourseTeacherDto();
        BeanUtils.copyProperties(courseTeacherDto, courseTeacherDtoNew);
        courseTeacherDtoNew.setCreateDate(LocalDateTime.now());
        Long id = courseTeacherDtoNew.getId();
        int rows = 0;
        if (id != null) {
            courseTeacherMapper.updateById(courseTeacherDtoNew);
        } else {
            rows = courseTeacherMapper.insert(courseTeacherDtoNew);

        }

        return rows > 0;
    }

    @Override
    public void delTeach(Long courseId, Long teacherId) {
        CourseTeacher courseTeacher = courseTeacherMapper.selectCourseById(courseId);
        CourseTeacherDto courseTeacherDto = new CourseTeacherDto();
        BeanUtils.copyProperties(courseTeacher,courseTeacherDto);
        if (courseTeacher.getCourseId() == null){
            XueChengPlusException.cast("查不到该课程的老师");
        }else{
            if (courseTeacherDto.getId() != null){
                courseTeacherMapper.deleteById(teacherId);
            }else {
                XueChengPlusException.cast("没有该老师");

            }
        }

    }

    @Override
    public void delCourse(Long CourseId) {
        teachplanMapper.deleteByCourseId(CourseId);
    }

    @Transactional
    @Override
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        //教学计划id
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if(teachplan==null){
            XueChengPlusException.cast("教学计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if(grade!=2){
            XueChengPlusException.cast("只允许第二级教学计划绑定媒资文件");
        }
        //课程id
        Long courseId = teachplan.getCourseId();

        //先删除原来该教学计划绑定的媒资
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId,teachplanId));

        //再添加教学计划与媒资的绑定关系
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMediaMapper.insert(teachplanMedia);
        return teachplanMedia;
    }

    @Override
    public TeachplanMedia delAssociationMedia(Long teachPlanId, String mediaId) {
        //删除该教学计划绑定的媒资
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId,teachPlanId)
                .eq(TeachplanMedia::getMediaId,mediaId));
        return null;
    }


    /**
     * @description 获取最新的排序号
     * @param courseId  课程id
     * @param parentId  父课程计划id
     * @return int 最新排序号
     * @author Mr.M
     * @date 2022/9/9 13:43
     */
    private int getTeachplanCount(long courseId,long parentId){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,courseId);
        queryWrapper.eq(Teachplan::getParentid,parentId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count;
    }
}