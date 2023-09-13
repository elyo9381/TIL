# 20211014-원요엘


# TIW( today i work )

- 다국어 쿼리수정
    - 항공명 및 항공(시티명)의 다국어 처리 진행( 쿼리 수정)
- MockK 의 이해 - 테스트코드작성(restAPI)
    
    # Controller Unit Test
    
     
    
   - controller code
        
        ```json
        @RestController
        @RequestMapping("/search")
        class SearchAction {
        
            @Autowired private lateinit var ZXCV:ZCXV
            @Autowired private lateinit var ADSF: ASDF
            @Autowired private lateinit var QWER: QWER
        
            private val logger = LoggerFactory.getLogger( SearchAction::class.java )
        
            @PostMapping("/check")
            fun check(@CurrentUser user:MemberDTO?, locale:Locale, @RequestBody search:ServiceSearchDTO):ResponseEntity<Any> {
                logger.error("회원 정보 : {}", user)
    
                search.locale = locale.language.toString()
        
                return ResponseEntity.ok( checkBiz.check(search) )
            }
        
            @PostMapping("/sale/list")
            fun saleList(@CurrentUser user:MemberDTO?, locale:Locale, @RequestBody search:ServiceSearchDTO):ResponseEntity<Any> {
  
                search.locale = locale.language.toString()
                return ResponseEntity.ok( ADSF.getSaleList(search) )
            }
        
            @PostMapping("/test")
            @Cacheable(cacheNames = ["artists"])
            fun test(@RequestBody search:ServiceSearchDTO):ResponseEntity<Any> {
                return ResponseEntity.ok( hashMapOf("data" to search) )
            }
        
        }
        ```
        
    
    위와같은 Act가 존재하고 이를 실행하려면 다양한 의존성이 필요합니다. 
    
     ex) ZXCV, ADSF....ETC
    
    그리고 ADSF를 주입하기 위해서는 다양한 의존성을 가집니다. 
    
    → 즉 컨트롤러를 테스트 하기 위해서는 모든 의존성이 존재해야 테스트 진행할수 있습니다. 
    
    우리의 목적은 spring을 서버에 올리지 않고 MockK을 이용해서 테스트 하고 싶기 때문입니다. 
    
   - controller Test code
        
        ```json
        @WebMvcTest(controllers = [SearchAction::class])
        @TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
        internal class SearchActionTest{
        
            private lateinit var webApplicationContext: WebApplicationContext
        
            private lateinit var mockMvc: MockMvc
        
            @MockkBean
            private lateinit var ADSF : ADSF
           .
           .
           .
           .
           .
           //여러가지 의존성 
        
            @BeforeEach
            internal fun setUp(webApplicationContext: WebApplicationContext) {
                mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                    .addFilter<DefaultMockMvcBuilder>(CharacterEncodingFilter("UTF-8", true))
                    .alwaysDo<DefaultMockMvcBuilder>(MockMvcResultHandlers.print())
                    .build()
            }
        
            @Test
            fun test123() {
        
            }
        
           
            @Test
            fun `test`() {
        
                val saleDTO = SaleDTO().apply {
                    this.type = "HOTEL"
                    this.code = "123123123"
                }
                var mutableList = mutableListOf<SaleDTO>()
        
                mutableList.add(saleDTO)
        
                val expectByCode = "$.[?(@.code == '%s')]";
        
                every { saleBiz.getSaleList( any() ) } returns mutableList
        
                for(i in 1..10){
                    mockMvc.perform(
                        MockMvcRequestBuilders.post("/search/sale/list")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-AUTH-TOKEN",token)
                            .content(
                                JsonObject().apply {
                                    this.addProperty("locale", "KO")
                                }.toString()
                            )
                    ).andExpect(MockMvcResultMatchers.status().isOk)
                        .andExpect(jsonPath(expectByCode,"123123123").exists())
                        .andExpect(jsonPath("$[0].code").value("123123123"))
                        .andReturn()
                }
        
            }
        
        }
        ```
        
1. 컨트롤러를 테스트하기 위해서는 @WebMvcTest(controllers = [테스트할 클래스]) 를 지정해줘야합니다.  우리는 스프링을 서버에 올리지 않기 때문입니다. 
2. 그리고 갖갖의 의존성을 추가해줘야합니다. 우리는 MockkBean을 이용해서 의존성을 주입할것입니다. 
3. MockkBean은 가짜 의존성 객체를 만드는 역할을 합니다. 
4. 그리고 매번 테스트할때마다 새로운 MockMvc를 만들어 객체의 의존성을 init하게 설정합니다. 
    
    ```kotlin
    @BeforeEach
        internal fun setUp(webApplicationContext: WebApplicationContext) {
            mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilter<DefaultMockMvcBuilder>(CharacterEncodingFilter("UTF-8", true))
                .alwaysDo<DefaultMockMvcBuilder>(MockMvcResultHandlers.print())
                .build()
        }
    ```
    
5. mockk의 스터빙 기능을 통해서 가짜 의존성 객체에 가짜 값을 주입할수있습니다.
    
    ```kotlin
    every { saleBiz.getSaleList( any() ) } returns mutableList
    ```
    

6. 이러한 방법을 통해 의존성을 설정하고 MockMvc를 통해 post로 날려보면 테스트 할 수 있습니다. 

    ```kotlin
    for(i in 1..10){
            mockMvc.perform(
                MockMvcRequestBuilders.post("/search/sale/list")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-AUTH-TOKEN",token)
                    .content(
                        JsonObject().apply {
                            this.addProperty("locale", "KO")
                        }.toString()
                    )
            ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(jsonPath(expectByCode,"123123123").exists())
                .andExpect(jsonPath("$[0].code").value("123123123"))
                .andReturn()
        }
   ```
    
   # Service Unit Test
    
   - service code
        
       ```kotlin
       @Service
       class SaleBizImpl : SaleBiz {
        
       //    @Autowired private lateinit var cacheManager: CacheManager
           @Autowired private lateinit var saleMapper: SaleMapper
        
           private val logger = LoggerFactory.getLogger(SaleBizImpl::class.java)
        
           @Cacheable(value = ["saleList"], keyGenerator = "simpleKeyGenerator")
       //    @Cacheable(cacheNames = ["testCache"])
           override fun getSaleList(search: ServiceSearchDTO): MutableList<SaleDTO> {
               logger.info("chche use ~~~~~")
               return saleMapper.getList(search)
           }
        
           override fun clear() {
               TODO("Not yet implemented")
           }
        
       //    override fun clear() {
       //        cacheManager.getCache("saleList")?.clear()
       //    }
       }
       ```
        
    
    서비스또한 여러가지 의존성이 존재해야 Mock을 통해서 테스트할수있습니다. 
    
   - service test code
        
        ```kotlin
        @ExtendWith(MockKExtension::class)
        internal class SaleBizImplTest {
        
            @MockK
            private lateinit var saleMapper: SaleMapper
        
            @MockK
            private lateinit var cacheManager: CacheManager
        
            @InjectMockKs
            private lateinit var ASDF:ASDF
        
            @BeforeEach
            internal fun setUp() {
                MockKAnnotations.init(this)
            }
        
            @Test
            fun getSaleList() {
                val saleDTO = SaleDTO().apply {
                    this.type = "HOTEL"
                    this.code = "123123123"
                }
                var mutableList = mutableListOf<SaleDTO>()
        
                mutableList.add(saleDTO)
        
                val search = ServiceSearchDTO().apply { this.type = "HOTEL" }
        
                every { saleMapper.getList(any()) } answers { mutableList }
        
                val saleList = saleBizImpl.getSaleList(search)
        
                assertEquals(saleList.size,1)
        
            }
        }
        ```
        
    1. 서비스 레이어를 테스트하기 위해서는 @ExtendWith(MockKextension::class)를 통해서  MockK를 사용한다고 명시해야합니다. 
    2. 그리고 서비스 테스트를위해서 각각의 의존성을 주입해야합니다. 
    3. 이때 테스트할 객체는 @InjectMockKs를 통해서 가짜MockK 을 주입받도록 설정합니다. 
    4. @Mock 를 통해서 가짜 객체를 만들수 있습니다. 
    5. 그리고 스터빙을 통해서 기능을 테스트합니다. 
    
   # JPA Unit Test
    
   - repository code
        
        ```kotlin
        interface MemberRepository : CrudRepository<MemberEntity,Int> {
        }
        ```
        
    
   레포지토리 테스트는 @DataJpaTest을 통해서 진행합니다. 
    
   - repository test code
        
        ```kotlin
        @DataJpaTest
        internal class MemberEntityTest(){
        
            val memberMapStruct = Mappers.getMapper(MemberMapStruct::class.java)
        
            @Autowired
            private lateinit var memberRepository: MemberRepository
        
            @Test
            fun createMemberEntity(){
        
                val memberDTO = MemberDTO().apply {
                    this.userId = "testId"
                    this.userPw = "testpw"
                    this.name = "testwon"
                    this.email = "test@mail.com"
                    this.gender = "M"
                    this.tel = "00011112222"
                }
        
                val toEntity = memberMapStruct.toEntity(memberDTO)
        
                assertNotNull(toEntity)
        
                val save = memberRepository.save(toEntity)
        
                assertNotNull(save)
                assertEquals("testId",save.userId)
            }
        }
        ```
        
    1. @DataJpaTest는 내장 디비를 기준으로 테스트합니다. 그렇기 때문에 h2로 테스트하였습니다. 
    2. 각각의 의존성을 설정하고 테스트를 진행합니다.