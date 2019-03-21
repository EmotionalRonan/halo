package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.entity.Category;
import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.model.entity.PostCategory;
import cc.ryanc.halo.repository.CategoryRepository;
import cc.ryanc.halo.repository.PostCategoryRepository;
import cc.ryanc.halo.repository.PostRepository;
import cc.ryanc.halo.service.PostCategoryService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import cc.ryanc.halo.utils.ServiceUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Post category service implementation.
 *
 * @author johnniang
 * @date 3/19/19
 */
@Service
public class PostCategoryServiceImpl extends AbstractCrudService<PostCategory, Integer> implements PostCategoryService {

    private final PostCategoryRepository postCategoryRepository;

    private final PostRepository postRepository;

    private final CategoryRepository categoryRepository;

    public PostCategoryServiceImpl(PostCategoryRepository postCategoryRepository,
                                   PostRepository postRepository,
                                   CategoryRepository categoryRepository) {
        super(postCategoryRepository);
        this.postCategoryRepository = postCategoryRepository;
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> listCategoryBy(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        // Find all category ids
        Set<Integer> categoryIds = postCategoryRepository.findAllCategoryIdsByPostId(postId);

        return categoryRepository.findAllById(categoryIds);
    }

    @Override
    public Map<Integer, List<Category>> listCategoryListMap(Collection<Integer> postIds) {
        if (CollectionUtils.isEmpty(postIds)) {
            return Collections.emptyMap();
        }

        // Find all post categories
        List<PostCategory> postCategories = postCategoryRepository.findAllByPostIdIn(postIds);

        // Fetch category ids
        Set<Integer> categoryIds = ServiceUtils.fetchProperty(postCategories, PostCategory::getCategoryId);

        // Find all categories
        List<Category> categories = categoryRepository.findAllById(categoryIds);

        // Convert to category map
        Map<Integer, Category> categoryMap = ServiceUtils.convertToMap(categories, Category::getId);

        // Create category list map
        Map<Integer, List<Category>> categoryListMap = new HashMap<>();

        // Foreach and collect
        postCategories.forEach(postCategory -> categoryListMap.computeIfAbsent(postCategory.getPostId(), postId -> new LinkedList<>()).add(categoryMap.get(postCategory.getCategoryId())));

        return categoryListMap;
    }

    @Override
    public List<Post> listPostBy(Integer categoryId) {
        Assert.notNull(categoryId, "Category id must not be null");

        // Find all post ids
        Set<Integer> postIds = postCategoryRepository.findAllPostIdsByCategoryId(categoryId);

        return postRepository.findAllById(postIds);
    }

    @Override
    public List<PostCategory> createBy(Integer postId, Set<Integer> categoryIds) {
        Assert.notNull(postId, "Post id must not be null");

        if (CollectionUtils.isEmpty(categoryIds)) {
            return Collections.emptyList();
        }

        // Build post categories
        List<PostCategory> postCategories = categoryIds.stream().map(categoryId -> {
            PostCategory postCategory = new PostCategory();
            postCategory.setPostId(postId);
            postCategory.setCategoryId(categoryId);
            return postCategory;
        }).collect(Collectors.toList());

        // Create them
        return createInBatch(postCategories);
    }
}