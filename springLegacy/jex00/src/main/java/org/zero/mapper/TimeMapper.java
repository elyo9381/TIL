package org.zero.mapper;

import org.apache.ibatis.annotations.Select;

public interface TimeMapper {

	
	@Select("SELECT sysdate FROM dual")
	public String getTime();
	
	
	// 리소스에서 xml파일을 설정해야하고 log4jdbc.log4j2.properties을 만들어 설정을 해줘야한다.
	public String getTime2();
}
