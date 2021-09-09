// 블루투스 & 초음파센서
#include <SoftwareSerial.h> //시리얼통신 라이브러리 호출

SoftwareSerial mySerial(2, 3); 
int trigPin = 4;
int echoPin = 5;


void setup(){
 Serial.begin(9600);
 mySerial.begin(9600); //블루투스 시리얼
 pinMode(trigPin, OUTPUT);
 pinMode(echoPin, INPUT);
}
 
//초음파를 보낸다. 다 보내면 echo가 HIGH(신호받기) 상태로 대기

void loop(){
  float duration, distance;
  digitalWrite(trigPin, HIGH);
  delay(10);
  digitalWrite(trigPin, LOW);
 
  // echoPin 이 HIGH를 유지한 시간을 저장한다.
  duration = pulseIn(echoPin, HIGH);
  // HIGH였을 때 시간(초음파가 보냈다가 다시 들어온 시간)을 가지고 거리를 계산한다.
  // 340은 초당 초음파(소리)의 속도, 10000은 밀리세컨드를 세컨드로, 왕복거리이므로 2로 나눠준다.
  distance = ((float)(340 * duration) / 10000) / 2;

 Serial.print("\nDIstance:");
 Serial.print(distance);
 Serial.println("cm\n");
 delay(500);
 if (mySerial.available()) {      
 Serial.write(mySerial.read());  //블루투스 측 내용을 시리얼 모니터에 출력
  }
 if (Serial.available()) {        
 mySerial.write(Serial.read());  //시리얼 모니터 내용을 블루투스 측에 WRITE
  }
}
