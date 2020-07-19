#include <SoftwareSerial.h>

#define DEBUG false

SoftwareSerial esp8266(4, 5); //make RX Arduino line is pin 7, make TX Arduino line is pin 8.

String clientAddr = "'192.168.4.2'";
String type = "'TCP'";
String port = "80";

boolean alreadyConnected = false;

int acelerar = 0;

void setup()
{
  while (!Serial);

  Serial.begin(9600);
  esp8266.begin(9600); // your esp's baud rate might be different

  pinMode(6, OUTPUT);
  digitalWrite(6, LOW);

  sendToEsp("AT+RST\r\n", 1000, DEBUG); // reset module
  sendToEsp("AT+CWMODE=2\r\n", 500, DEBUG); // configure as access point
  sendToEsp("AT+CIFSR\r\n", 500, DEBUG); // get ip address
  sendToEsp("AT+CIPMUX=1\r\n", 500, DEBUG); // configure for multiple connections
  sendToEsp("AT+CIPSERVER=1,80\r\n", 500, DEBUG); // turn on server on port 80
  //sendToEsp("AT+UART_DEF=9600,8,1,0,0\r\n", 2000, DEBUG);
  // Cambia velocidad de modulo ES... NO USAR SI NO SABES QUE HACE!!!
  Serial.println("Access Point Ready!");
}

void loop()
{
  if (esp8266.available()) // check if the esp is sending a message
  {
    if (!alreadyConnected) {
      // clear out the input buffer:
      alreadyConnected = true;

      Serial.println("New client!");
    }

    if (esp8266.find("+IPD,"))
    {
      delay(200);
      int connectionId = esp8266.read() - 48;

      esp8266.find("acelerar="); // advance cursor to "pin="

      acelerar = (esp8266.read() - 48);
      Serial.println("acelerar: ");
      Serial.println(acelerar);
      if (acelerar == 1) {
        sendHttpResponse(connectionId);
        digitalWrite(6, HIGH);
      } else if (acelerar == 0) {
        sendHttpResponse(connectionId);
        digitalWrite(6, LOW);
      }
      //String closeCommand = "AT+CIPCLOSE=";
      //closeCommand += connectionId; // append connection id
      //closeCommand += "\r\n";
    }
  }
}

void sendHttpResponse(int connectionId)
{
  // make send command
  String content = "Respuesta HTTP!";
  String httpResponse = "";
  String httpHeader = "";
  // HTTP Header
  httpHeader = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=UTF-8\r\n";
  httpHeader += "Content-Length: ";
  httpHeader += content.length();
  httpHeader += "\r\n";
  httpHeader += "Connection: Closed\r\n\r\n";
  httpResponse = httpHeader + content + " ";
  // cipSend configuration
  String cipSend = "AT+CIPSEND=";
  cipSend += connectionId;
  cipSend += ",";
  cipSend += httpResponse.length();
  cipSend += "\r\n";
  sendToEsp(cipSend, 10, DEBUG);
  sendData(httpResponse, 10, DEBUG);
}
/*
  Name: sendCommand
  Description: Function used to send data to ESP8266.
  Params: command - the data/command to send; timeout - the time to wait for a response; debug - print to Serial window?(true = yes, false = no)
  Returns: The response from the esp8266 (if there is a reponse)
*/
String sendToEsp(String command, const int timeout, boolean debug)
{
  String response = "";

  esp8266.print(command); // send the read character to the esp8266

  long int time = millis();

  while ( (time + timeout) > millis())
  {
    while (esp8266.available() > 0)
    {
      // The esp has data so display its output to the serial window
      char c = (char)esp8266.read(); // read the next character.
      response += c;
    }
  }

  if (debug)
  {
    Serial.print(response);
  }
  return response;
}
/*
  Name: sendData
  Description: Function used to send data to ESP8266.
  Params: command - the data/command to send; timeout - the time to wait for a response; debug - print to Serial window?(true = yes, false = no)
  Returns: The response from the esp8266 (if there is a reponse)
*/
String sendData(String command, const int timeout, boolean debug)
{
  String response = "";

  int dataSize = command.length();
  char data[dataSize];
  command.toCharArray(data, dataSize);

  esp8266.write(data, dataSize); // send the read character to the esp8266
  if (debug)
  {
    Serial.println("\r\n====== HTTP Response From Arduino ======");
    Serial.write(data, dataSize);
    Serial.println("\r\n========================================");
  }
  long int time = millis();
  while ( (time + timeout) > millis())
  {
    while (esp8266.available())
    {
      // The esp has data so display its output to the serial window
      char c = esp8266.read(); // read the next character.
      response += c;
    }
  }
  if (debug)
  {
    Serial.print(response);
  }
  return response;
}
