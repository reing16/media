package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.*;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description 课程计划编辑接口
 * @author Mr.M
 * @date 2022/9/6 11:29
 * @version 1.0
 */
@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
@RestController
public class TeachplanController {
    @Autowired
    TeachplanService teachplanService;


    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId",name = "课程Id",required = true,dataType = "Long",paramType = "path")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId){
        return teachplanService.findTeachplanTree(courseId);
    }


    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan( @RequestBody SaveTeachplanDto teachplan){
        teachplanService.saveTeachplan(teachplan);
    }

    @ApiOperation("课程计划删除")
    @DeleteMapping("/teachplan/{planId}")
    public void delTeachplan(@PathVariable Long planId){
         teachplanService.delTeachplan(planId);
    }

    @ApiOperation("课程计划上移")
    @PostMapping("/teachplan/moveup/{planId}")
    public void moveUp(@PathVariable Long planId){
        teachplanService.moveUp(planId);
    }

    @ApiOperation("课程计划下移")
    @PostMapping("/teachplan/movdown/{planId}")
    public void movdown(@PathVariable Long planId){
        teachplanService.movdown(planId);
    }

    @ApiOperation("查询教师")
    @GetMapping("/courseTeacher/list/{courseId}")
    public CourseTeacherDto getcourseTeacher(@PathVariable Long courseId){
        return teachplanService.findcourseTeacher(courseId);
    }
    @ApiOperation("添加或修改教师")
    @PostMapping("/courseTeacher")
    public boolean addCourseTeacher(@RequestBody @Validated CourseTeacherDto courseTeacherDto){
        Long companyId = 1232141488L;
        return teachplanService.addCourseTeacher(companyId,courseTeacherDto);
    }

    @ApiOperation("教师删除")
    @DeleteMapping("/ourseTeacher/course/{courseId}/{teacherId}")
    public void delTeach(@PathVariable("courseId") Long courseId, @PathVariable("teacherId")Long teacherId){
        teachplanService.delTeach(courseId,teacherId);
    }

    @ApiOperation("课程删除")
    @DeleteMapping("/course/{courseId}")
    public void delCourse(@PathVariable Long courseId){
        teachplanService.delCourse(courseId);
    }

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto){
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }

    @ApiOperation("课程删除")
    @DeleteMapping("/teachplan/association/media/{teachPlanId}/{mediaId}")
    public void delAssociationMedia(@PathVariable("teachPlanId") Long teachPlanId,@PathVariable("mediaId") String mediaId){
        teachplanService.delAssociationMedia(teachPlanId,mediaId);
    }

}