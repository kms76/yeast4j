# yeast4j
yeast4j is simple library for convert byte stream to message object

# 소개  
    Yeast4J는 바이트 스트림을 사용자가 정의한 format정보에 맞게 엔코딩/디코딩을 하여 Message Object로 변환합니다.  
    
    예를들어 TCP/IP 프로그래밍을 할때 우리는 프로토콜(message format)을 정의 합니다.  
    client가 메시지를 보내면 서버는 byte stream을 읽습니다. 그리고 정의된 프로토콜로 파싱합니다.
    이때 YeastJ를 사용하면 간단히 정의된 format의 Message Object를 얻을수 있습니다.
   
 # 사용 방법 
    
    - format 정의 하기 
        Yeast4J를 사용하기 위해서는 먼저 format 정의가 필요합니다.
        텍스트 파일을 열고 아래와 같이 작성후 확장자를 '.fmt'로 저장 하세요.
        
       Chat_Login_Req.fmt 파일 내용
       
       Chat_Login_Req {
          length:%4d,
          id:%20s,
          pwd:%20s,
          filler:%6s
        }
        
        정의된 format은 4개의 필더로 구성된 메시지 입니다. length는 4byte의 '0050' 형태의 값입니다.
        필더 정의 형식은 java의 String format과 유사합니다.
        '%' 다음의 숫자는 필더 길이를 뜻하고 'd'는 정수를 뜻합니다. 's'는 string을 표현합니다.
        
   - Yeast4J 사용  
      주요 component 설명  
        MessageFormatParser: format 정보를 입력받아서 MessageFormat 객체를 생성합니다.  
        MessageFormat: format 정보를 tree 자료 구조로 표현합니다. 
        Message: 메시지는 format을 가지며, 정보를 get, set 할 수 있습니다.  
        MessageEncoder: Message to byte stream  
        MessageDecoder: byte stream to Message  
        
        ```
        // 메시지 파서 생성
        MessageFormatParser mfp = new MessageFormatParser();
        BufferedReader reader = null;
		
        try {
          // format 정보를 읽어서 파서에 준다.
          reader = new BufferedReader(new FileReader("C:/Chat_Login_Req.fmt"));
          
          // format 정보를 파싱한다.
          MessageFormat format = mfp.parse(reader);
          
          // 메시지 생성 및 값 설정
          Message message = new Message(format);
          message.setInt("length", 50);
          message.setString("id", "kms");
          message.setString("pwd", "1234!!");
          
          // 메시지를 바이트 스트림으로 엔코딩 한다.
          MessageEncoder encoder = new MessageEncoder();
          byte[] result = encoder.encode(message);
	  // 결과[0050kms                 1234!!                    ]

          // 바이트 스트림을 메시지로 디코딩 한다.
          MessageDecoder decoder = new MessageDecoder(format);
          Message msg = decoder.decode(result);
        } catch(IOException e) {
          System.out.println(e.getMessage());
        }
       ```
        
        
