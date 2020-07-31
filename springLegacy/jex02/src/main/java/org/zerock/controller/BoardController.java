package org.zerock.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zerock.domain.BoardVO;
import org.zerock.service.BoardService;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@RequestMapping("/board/**")
@Controller
public class BoardController { 
	
	@Setter(onMethod_ = @Autowired)
	private BoardService service;

	 @GetMapping("/list")
	 public void list(Model model) {
	
	 log.info("list");
	 log.debug("list");
	 model.addAttribute("list", service.getList());
	
	 }

	// RedirectAttributes 추가적으로 새롭게 등록된 게시물의 번호를 같이전달하기 위해서
	@PostMapping("/register")
	public String register(BoardVO board, RedirectAttributes rttr) {
		log.info("register : " + board);
		service.register(board);
		rttr.addFlashAttribute("result", board.getBno());
		return "redirect:/board/list";
	}

	// 하나의값만 보여줄때
	// bno 값을 명시적으러 처리하는 @RequestParam을 이용한다.
	// 화면쪽으로 해당 번호의 게시물을 전달해야 하므로 Model파라미터 지
	@GetMapping("/get")
	public void get(@RequestParam("bno") Long bno, Model model) {

		log.info("/get");
		model.addAttribute("board", service.get(bno));

	}

	@PostMapping("/modify")
	public String modify(BoardVO board, RedirectAttributes rttr) {
		log.info("modify : " + board);
		if (service.modify(board)) {
			rttr.addFlashAttribute("result", "success");
		}
		return "redirect:/board/list";
	}

	@PostMapping("/remove")
	public String remove(@RequestParam("bno") Long bno, RedirectAttributes rttr) {
		log.info("remove : " + bno);

		if (service.remove(bno)) {
			rttr.addFlashAttribute("result", "success");
		}
		return "redirection:/board/remove";
	}

}
