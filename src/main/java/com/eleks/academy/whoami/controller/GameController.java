package com.eleks.academy.whoami.controller;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.model.request.*;
import com.eleks.academy.whoami.model.response.GameHistory;
import com.eleks.academy.whoami.model.response.AllFields;
import com.eleks.academy.whoami.model.response.GameDetails;
import com.eleks.academy.whoami.model.response.GameShortInfo;
import com.eleks.academy.whoami.model.response.LeaveDetails;
import com.eleks.academy.whoami.model.response.TurnDetails;
import com.eleks.academy.whoami.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.eleks.academy.whoami.utils.StringUtils.Headers.PLAYER;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<GameShortInfo> findAvailableGames(@RequestHeader(PLAYER) String player) {
        return this.gameService.findAvailableGames(player);
    }

    @GetMapping("/info")
    public List<AllFields> findAllGamesInfo(@RequestHeader(PLAYER) String player) {
        return this.gameService.findAllGamesInfo(player);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<GameDetails> createGame(@RequestHeader(PLAYER) String player,
                                                  @Valid @RequestBody NewGameSize gameRequest) {

        return this.gameService.createGame(player, gameRequest)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GameDetails> findById(@PathVariable("id") String id,
                                                @RequestHeader(PLAYER) String player) {

        return this.gameService.findByIdAndPlayer(id, player)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/players")
    public SynchronousPlayer enrollToGame(@PathVariable("id") String id,
                                          @RequestHeader(PLAYER) String player) {
        return this.gameService.enrollToGame(id, player);
    }

    @PostMapping("/{id}/characters")
    @ResponseStatus(HttpStatus.OK)
    public void suggestCharacter(@PathVariable("id") String id,
                                 @RequestHeader(PLAYER) String player,
                                 @Valid @RequestBody CharacterSuggestion suggestion) {

        this.gameService.suggestCharacter(id, player, suggestion);
    }

    @GetMapping("/{id}/turn")
    public ResponseEntity<TurnDetails> findTurnInfo(@PathVariable("id") String id,
                                                    @RequestHeader(PLAYER) String player) {

        return this.gameService.findTurnInfo(id, player)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/question")
    public void askQuestion(@PathVariable("id") String id,
                            @RequestHeader(PLAYER) String player,
                            @RequestBody Message message) {

        this.gameService.askQuestion(id, player, message.getMessage());
    }

    @PostMapping("/{id}/question/answer")
    public void answerQuestion(@PathVariable("id") String id,
                               @RequestHeader(PLAYER) String player,
                               @RequestParam QuestionAnswer answer) {

        this.gameService.answerQuestion(id, player, answer);

    }

    @PostMapping("/{id}/guess")
    public void submitGuess(@PathVariable("id") String id,
                            @RequestHeader(PLAYER) String player,
                            @RequestBody Message message) {

        this.gameService.submitGuess(id, player, message.getMessage());
    }

    @PostMapping("/{id}/guess/answer")
    public void answerGuess(@PathVariable("id") String id,
                            @RequestHeader(PLAYER) String player,
                            @RequestParam GuessAnswer answer) {

        this.gameService.answerGuess(id, player, answer);

    }

    @GetMapping("/{id}/history")
    public ResponseEntity<GameHistory> getGameHistory(@PathVariable("id") String id) {
        return this.gameService.findGameHistory(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/leave")
    public ResponseEntity<LeaveDetails> leaveGame(@PathVariable("id") String id,
                                                  @RequestHeader(PLAYER) String player) {

        return this.gameService.leaveGame(id, player)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/all-players-count")
    public Integer getAllPlayersCount(@RequestHeader(PLAYER) String player) {
        return this.gameService.getAllPlayersCount();
    }

    @PostMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GameDetails> startGame(@PathVariable("id") String id,
                                                 @RequestHeader(PLAYER) String player) {

        return this.gameService.startGame(id, player)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
