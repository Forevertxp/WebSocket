//////////////////////////////////////////////////////////////////////////////////////////////////
//
//  ViewController.m
//  SimpleTest
//
//  Created by Austin and Dalton Cherry on on 2/24/15.
//  Copyright (c) 2014-2017 Austin Cherry.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
//////////////////////////////////////////////////////////////////////////////////////////////////

#import "ViewController.h"
#import "JFRWebSocket.h"

@interface ViewController ()<JFRWebSocketDelegate>

@property(nonatomic, strong)JFRWebSocket *socket;

@end

@implementation ViewController{
    NSString *tokenString;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.socket = [[JFRWebSocket alloc] initWithURL:[NSURL URLWithString:@"ws://192.168.9.135:7397"] protocols:nil];
    self.socket.delegate = self;
    [self.socket connect];
}

// pragma mark: WebSocket Delegate methods.

-(void)websocketDidConnect:(JFRWebSocket*)socket {
    NSLog(@"websocket is connected");
}

-(void)websocketDidDisconnect:(JFRWebSocket*)socket error:(NSError*)error {
    NSLog(@"websocket is disconnected: %@", [error localizedDescription]);
        [self.socket connect];
}

-(void)websocket:(JFRWebSocket*)socket didReceiveMessage:(NSString*)string {
    NSLog(@"Received text: %@", string);
    NSDictionary *result = [self dictionaryWithJsonString:string];
    int type = [[result objectForKey:@"type"] intValue];
    if (type==1) {
        NSString *body = [result objectForKey:@"body"];
        NSDictionary *token = [self dictionaryWithJsonString:body];
        tokenString = [token objectForKey:@"token"];
        [self sendMessage];
        [NSTimer scheduledTimerWithTimeInterval:10.0 target:self selector:@selector(sendHeartbeat) userInfo:nil repeats:YES];
        
//        [NSTimer scheduledTimerWithTimeInterval:5.0 target:self selector:@selector(createGroup) userInfo:nil repeats:NO];
    }
    if (type==6) {
        [NSTimer scheduledTimerWithTimeInterval:30.0 target:self selector:@selector(logout) userInfo:nil repeats:NO];
    }
}

-(void)websocket:(JFRWebSocket*)socket didReceiveData:(NSData*)data {
    NSLog(@"Received data: %@", data);
}

// pragma mark: target actions.

// 登录
- (IBAction)writeText:(UIBarButtonItem *)sender {
    [self register];
    //[self login];
}

- (void)register {
    NSDictionary *reg = [NSDictionary dictionaryWithObjectsAndKeys:@"13581574738",@"phone",@"123456",@"password", nil];;
    NSDictionary *dic = [NSDictionary dictionaryWithObjectsAndKeys:@"1",@"sign",@"2",@"type",reg,@"body", nil];
    [self.socket writeString:[self dataToJsonString:dic]];
    
}

-(void)login{
    NSDictionary *login = [NSDictionary dictionaryWithObjectsAndKeys:@"13581574738",@"phone",@"123456",@"password", nil];;
    NSDictionary *dic = [NSDictionary dictionaryWithObjectsAndKeys:@"1",@"sign",@"1",@"type",login,@"body", nil];
    [self.socket writeString:[self dataToJsonString:dic]];
}



//心跳包
- (void)sendHeartbeat{
    NSDictionary *send = [NSDictionary dictionaryWithObjectsAndKeys:@"1",@"sign",@"18",@"type",@"",@"body", nil];
    [self.socket writeString:[self dataToJsonString:send]];
}

- (void)createGroup{
    NSDictionary *message = [NSDictionary dictionaryWithObjectsAndKeys:@"13581574738",@"phone",@"test",@"groupName",nil];
    NSDictionary *send = [NSDictionary dictionaryWithObjectsAndKeys:@"1",@"sign",@"7",@"type",message,@"body", nil];
    [self.socket writeString:[self dataToJsonString:send]];
}

- (void)addMember{
    NSDictionary *message = [NSDictionary dictionaryWithObjectsAndKeys:@"13581574738",@"phone",@"test",@"groupName",@"13621285412",@"member",nil];
    NSDictionary *send = [NSDictionary dictionaryWithObjectsAndKeys:@"1",@"sign",@"9",@"type",message,@"body", nil];
    [self.socket writeString:[self dataToJsonString:send]];
}

//发送消息
- (void)sendMessage{
    NSString *timestamp = [self getNowTimeTimestamp];
    NSDictionary *message = [NSDictionary dictionaryWithObjectsAndKeys:@"13581574738",@"sender",@"test",@"receiver",@"测试",@"content",timestamp,@"time", nil];
    NSDictionary *send = [NSDictionary dictionaryWithObjectsAndKeys:@"1",@"sign",@"6",@"type",message,@"body", nil];
    [self.socket writeString:[self dataToJsonString:send]];
}

// 退出登录
- (void)logout {
    NSDictionary *logout = [NSDictionary dictionaryWithObjectsAndKeys:@"13581574738",@"phone",tokenString,@"token", nil];;
    NSDictionary *dic = [NSDictionary dictionaryWithObjectsAndKeys:@"1",@"sign",@"3",@"type",logout,@"body", nil];
    [self.socket writeString:[self dataToJsonString:dic]];
    
}

-(NSString *)getNowTimeTimestamp{
    NSDate* dat = [NSDate dateWithTimeIntervalSinceNow:0];
    NSTimeInterval a=[dat timeIntervalSince1970];
    NSString*timeString = [NSString stringWithFormat:@"%0.f", a];//转为字符型
    return timeString;
}

- (NSDictionary *)dictionaryWithJsonString:(NSString *)jsonString
{
    if (jsonString == nil) {
        return nil;
    }
    
    NSData *jsonData = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    NSError *err;
    NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:jsonData
                                                        options:NSJSONReadingMutableContainers
                                                          error:&err];
    if(err)
    {
        NSLog(@"json解析失败：%@",err);
        return nil;
    }
    return dic;
}

- (IBAction)disconnect:(UIBarButtonItem *)sender {
    if(self.socket.isConnected) {
        sender.title = @"Connect";
        [self.socket disconnect];
    } else {
        sender.title = @"Disconnect";
        [self.socket connect];
    }
}

-(NSString*)dataToJsonString:(id)object
{
    NSString *jsonString = nil;
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:object
                                                       options:NSJSONWritingPrettyPrinted
                                                         error:&error];
    if (! jsonData) {
        NSLog(@"Got an error: %@", error);
    } else {
        jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    }
    return jsonString;
}

@end
