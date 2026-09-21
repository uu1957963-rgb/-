package me.user.minervacore.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class HelpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "========== [ MinervaCore 관리자 명령어 목록 ] ==========");
        sender.sendMessage(ChatColor.YELLOW + "/명령어 " + ChatColor.WHITE + "- 서버의 모든 관리자 명령어를 확인합니다. (OP)");
        sender.sendMessage(ChatColor.YELLOW + "/대기 " + ChatColor.WHITE + "- 20x20 보더 축소 및 블록 설치/파괴 방지 대기실 모드 (OP)");
        sender.sendMessage(ChatColor.YELLOW + "/청키 시작 [반경] " + ChatColor.WHITE + "- 렉 방지용 청크 사전 로딩 (예: /청키 시작 1000) (OP)");
        sender.sendMessage(ChatColor.YELLOW + "/청키 중지 " + ChatColor.WHITE + "- 진행 중인 청크 사전 생성을 중지합니다. (OP)");
        sender.sendMessage(ChatColor.YELLOW + "/자원 [시간] [보더] [X] [Y] [Z] " + ChatColor.WHITE + "- 지정 좌표 이동, 평화로움 난이도, 자원시간 시작 (OP)");
        sender.sendMessage(ChatColor.YELLOW + "/템 " + ChatColor.WHITE + "- 곡괭이(효율10/내구3/행운7), 겉날개, 작업도구 세트, 200레벨 지급 (OP)");
        sender.sendMessage(ChatColor.YELLOW + "/무적모드 풀기 " + ChatColor.WHITE + "- 진행 중인 무적 상태를 즉시 강제 해제합니다. (OP)");
        sender.sendMessage(ChatColor.YELLOW + "/1등 [닉네임] " + ChatColor.WHITE + "- 지정한 유저의 우승 타이틀 및 축하 폭죽을 출력합니다. (OP)");
        sender.sendMessage(ChatColor.YELLOW + "/팀 구성 [인원수] " + ChatColor.WHITE + "- 온라인 플레이어를 무작위 팀 배정합니다. (OP)");
        sender.sendMessage(ChatColor.YELLOW + "/팀 지정 [닉1] [닉2]... " + ChatColor.WHITE + "- 특정 플레이어들을 수동 팀 배정합니다. (OP)");
        sender.sendMessage(ChatColor.YELLOW + "/팀 초기화 " + ChatColor.WHITE + "- 생성된 모든 팀 설정을 초기화합니다. (OP)");
        sender.sendMessage(ChatColor.YELLOW + "/초기화 " + ChatColor.WHITE + "- 모든 타이머, 보더, 무적, 킬수, 상태 전체 리셋 (OP)");
        sender.sendMessage(ChatColor.YELLOW + "/발광 [닉네임 / all] " + ChatColor.WHITE + "- 팀 색상 발광 부여 (토템 발동 후에도 유지) (OP)");
        sender.sendMessage(ChatColor.YELLOW + "/아이피 확인 [닉네임] " + ChatColor.WHITE + "- 유저 IP 실시간 및 DB 기록 확인 (OP)");
        sender.sendMessage(ChatColor.YELLOW + "/롤백 [시간] " + ChatColor.WHITE + "- CoreProtect 연동 서버 롤백 (OP)");
        sender.sendMessage(ChatColor.AQUA + "[일반 유저 가능 명령어]");
        sender.sendMessage(ChatColor.GREEN + "/수선 " + ChatColor.WHITE + "- 손에 든 아이템에 수선 인챈트 부여");
        sender.sendMessage(ChatColor.GREEN + "/가시제거 " + ChatColor.WHITE + "- 손에 든 방어구에서 가시 인챈트 제거");
        sender.sendMessage(ChatColor.GREEN + "/tp [팀원] " + ChatColor.WHITE + "- 자원 시간 동안 팀원에게 1회 이동");
        sender.sendMessage(ChatColor.GREEN + "/top " + ChatColor.WHITE + "- 지상 가장 높은 블록으로 텔레포트");
        sender.sendMessage(ChatColor.GOLD + "========================================================");
        return true;
    }
}
