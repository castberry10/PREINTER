import React from 'react';

import styled from 'styled-components';

const NotFoundBlock = styled.div`

`;

const NotFoundErrorPage = () => {
  return (
    <div>
		  {/* <HeaderContainer/> */}
		  <NotFoundBlock>
			  {/* <img src = 'img/shipimg.png' width="500px"/> */}
			  <br/>
			  존재하지않는 페이지입니다!<br/>
			  주소를 다시 확인해주세요!<br/>
		  </NotFoundBlock>
		  {/* <Footer/> */}
	 </div>
  );
};

export default NotFoundErrorPage;
