package org.lkf.agent.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.lkf.agent.entity.ToolManifestSnapshotEntity;

@Mapper
public interface ToolManifestSnapshotMapper {

    @Insert("insert into tool_manifest_snapshot(id, tool_def_id, manifest_json, manifest_hash, fetched_at, expire_at, status, error_message, created_at, updated_at, del_flag) " +
            "values(#{id}, #{toolDefId}, cast(#{manifestJson} as jsonb), #{manifestHash}, #{fetchedAt}, #{expireAt}, #{status}, #{errorMessage}, now(), now(), 0)")
    int insert(ToolManifestSnapshotEntity entity);

    @Update("update tool_manifest_snapshot set del_flag = 1, updated_at = now() where tool_def_id = #{toolDefId} and del_flag = 0")
    int softDeleteByToolDefId(@Param("toolDefId") String toolDefId);

    @Select("select id, tool_def_id as toolDefId, manifest_json::text as manifestJson, manifest_hash as manifestHash, " +
            "fetched_at at time zone 'UTC' as fetchedAt, expire_at at time zone 'UTC' as expireAt, status, error_message as errorMessage, " +
            "created_at at time zone 'UTC' as createdAt, updated_at at time zone 'UTC' as updatedAt, del_flag as delFlag " +
            "from tool_manifest_snapshot where tool_def_id = #{toolDefId} and del_flag = 0 order by fetched_at desc limit 1")
    ToolManifestSnapshotEntity findLatestByToolDefId(@Param("toolDefId") String toolDefId);
}
